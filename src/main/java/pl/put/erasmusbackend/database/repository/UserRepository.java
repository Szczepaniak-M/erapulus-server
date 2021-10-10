package pl.put.erasmusbackend.database.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.put.erasmusbackend.database.model.ApplicationUser;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<ApplicationUser, Integer> {

    @Query("SELECT * FROM ApplicationUser WHERE email = :email")
    Mono<ApplicationUser> findByEmail(@Param("email") String email);
}
