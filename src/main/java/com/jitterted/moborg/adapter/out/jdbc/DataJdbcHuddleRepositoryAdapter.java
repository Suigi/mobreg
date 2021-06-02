package com.jitterted.moborg.adapter.out.jdbc;

import com.jitterted.moborg.domain.Huddle;
import com.jitterted.moborg.domain.HuddleId;
import com.jitterted.moborg.domain.HuddleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DataJdbcHuddleRepositoryAdapter implements HuddleRepository {

    private final JdbcHuddleRepository jdbcHuddleRepository;

    @Autowired
    public DataJdbcHuddleRepositoryAdapter(JdbcHuddleRepository jdbcHuddleRepository) {
        this.jdbcHuddleRepository = jdbcHuddleRepository;
    }

    @Override
    public Huddle save(Huddle huddle) {
        return null;
    }

    @Override
    public List<Huddle> findAll() {
        return null;
    }

    @Override
    public Optional<Huddle> findById(HuddleId huddleId) {
        return Optional.empty();
    }
}
