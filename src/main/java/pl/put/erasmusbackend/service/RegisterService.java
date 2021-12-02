package pl.put.erasmusbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import pl.put.erasmusbackend.database.repository.EmployeeRepository;
import pl.put.erasmusbackend.dto.EmployeeCreateRequestDto;
import pl.put.erasmusbackend.dto.EmployeeCreatedDto;
import pl.put.erasmusbackend.mapper.EmployeeCreateRequestToEmployeeEntityMapper;
import pl.put.erasmusbackend.mapper.EmployeeEntityToEmployeeCreatedDtoMapper;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class RegisterService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmployeeRepository employeeRepository;
    private final EmployeeCreateRequestToEmployeeEntityMapper employeeCreateRequestToEmployeeEntityMapper;
    private final EmployeeEntityToEmployeeCreatedDtoMapper employeeEntityToEmployeeCreatedDtoMapper;

    public Mono<EmployeeCreatedDto> createEmployee(@Valid EmployeeCreateRequestDto employeeCreateRequestDto) {
        String encryptedPassword = bCryptPasswordEncoder.encode(employeeCreateRequestDto.password());
        employeeCreateRequestDto.password("");
        return Mono.just(employeeCreateRequestDto)
                   .map(employeeCreateRequestToEmployeeEntityMapper::from)
                   .map(employee -> employee.password(encryptedPassword))
                   .flatMap(employeeRepository::save)
                   .map(employeeEntityToEmployeeCreatedDtoMapper::from)
                   .doOnError(DataIntegrityViolationException.class, e -> log.info("Duplicated email for request:" + employeeCreateRequestDto))
                   .doOnError(e -> log.info("Unexpected error" + e.getMessage()));
    }
}
