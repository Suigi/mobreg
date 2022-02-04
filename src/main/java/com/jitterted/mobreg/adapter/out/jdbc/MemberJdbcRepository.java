package com.jitterted.mobreg.adapter.out.jdbc;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberJdbcRepository extends CrudRepository<MemberDbo, Long> {
    Optional<MemberDbo> findByGithubUsername(String githubUsername);
}
