package pl.put.erasmusbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.put.erasmusbackend.database.model.UniversityEntity;
import pl.put.erasmusbackend.database.repository.UniversityRepository;
import pl.put.erasmusbackend.dto.UniversityListDto;
import pl.put.erasmusbackend.dto.UniversityRequestDto;
import pl.put.erasmusbackend.dto.UniversityResponseDto;
import pl.put.erasmusbackend.mapper.EntityToResponseDtoMapper;
import pl.put.erasmusbackend.mapper.RequestDtoToEntityMapper;
import pl.put.erasmusbackend.mapper.UniversityEntityToUniversityListDtoMapper;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Service
@Validated
public class UniversityService extends CrudGenericService<UniversityEntity, UniversityRequestDto, UniversityResponseDto> {

    private final UniversityRepository universityRepository;
    private final UniversityEntityToUniversityListDtoMapper universityEntityToUniversityListDtoMapper;

    public UniversityService(UniversityRepository universityRepository,
                             RequestDtoToEntityMapper<UniversityRequestDto, UniversityEntity> requestDtoToEntityMapper,
                             EntityToResponseDtoMapper<UniversityEntity, UniversityResponseDto> entityToResponseDtoMapper,
                             UniversityEntityToUniversityListDtoMapper universityEntityToUniversityListDtoMapper) {
        super(universityRepository, requestDtoToEntityMapper, entityToResponseDtoMapper);
        this.universityRepository = universityRepository;
        this.universityEntityToUniversityListDtoMapper = universityEntityToUniversityListDtoMapper;
    }

    public Mono<List<UniversityListDto>> listEntities() {
        return universityRepository.findAllUniversitiesNameAndId()
                                   .map(universityEntityToUniversityListDtoMapper::from)
                                   .collectList();
    }

    public Mono<UniversityResponseDto> createEntity(@Valid UniversityRequestDto requestDto) {
        UnaryOperator<UniversityEntity> addParamFromPath = university -> university;
        return createEntity(requestDto, addParamFromPath);
    }

    public Mono<UniversityResponseDto> getEntityById(int universityId) {
        Supplier<Mono<UniversityEntity>> supplier = () -> universityRepository.findById(universityId);
        return getEntityById(supplier);
    }

    public Mono<UniversityResponseDto> updateEntity(@Valid UniversityRequestDto requestDto, int universityId) {
        UnaryOperator<UniversityEntity> addParamFromPath = university -> university.id(universityId);
        return updateEntity(requestDto, addParamFromPath);
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteEntity(int universityId) {
        // TODO Add deleting Posts, Building, Documents, Programs and Modules,
        return universityRepository.findById(universityId)
                                   .switchIfEmpty(Mono.error(new NoSuchElementException()))
                                   .flatMap(b -> universityRepository.deleteById(universityId))
                                   .thenReturn(true);
    }
}
