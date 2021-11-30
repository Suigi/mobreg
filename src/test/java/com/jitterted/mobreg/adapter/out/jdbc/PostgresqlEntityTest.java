package com.jitterted.mobreg.adapter.out.jdbc;

import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest()
@Tag("container")
@Tag("integration")
class PostgresqlEntityTest {

    @Autowired
    HuddleJdbcRepository huddleJdbcRepository;

    @Autowired
    MemberJdbcRepository memberJdbcRepository;

    // create shared container with a container image name "postgres" and latest major release of PostgreSQL "13"
    @Container
    public static PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("posttest")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.sql.init.platform", () -> "postgresql");
    }

    @Test
    public void huddleEntityStoredViaJdbcIsRetrievedWithMembers() throws Exception {
        Ensemble original = new Ensemble("entity", ZonedDateTime.of(2021, 1, 3, 0, 0, 0, 0, ZoneId.systemDefault()));
        original.acceptedBy(MemberId.of(4L));
        original.acceptedBy(MemberId.of(5L));
        original.declinedBy(MemberId.of(73L));
        original.declinedBy(MemberId.of(79L));
        HuddleEntity originalEntity = HuddleEntity.from(original);

        HuddleEntity savedEntity = huddleJdbcRepository.save(originalEntity);

        Optional<HuddleEntity> retrievedEntity = huddleJdbcRepository.findById(savedEntity.getId());

        assertThat(retrievedEntity)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(originalEntity);

        assertThat(retrievedEntity.get().getAcceptedMembers())
                .extracting(AcceptedMember::asMemberId)
                .extracting(MemberId::id)
                .containsOnly(4L, 5L);

        assertThat(retrievedEntity.get().getDeclinedMembers())
                .extracting(DeclinedMember::asMemberId)
                .extracting(MemberId::id)
                .containsOnly(73L, 79L);
    }

    @Test
    public void memberEntityStoredViaJdbcIsRetrievedAsOriginal() throws Exception {
        Member member = new Member("firstName", "github");

        MemberEntity originalEntity = MemberEntity.from(member);

        MemberEntity savedEntity = memberJdbcRepository.save(originalEntity);

        Optional<MemberEntity> retrievedEntity = memberJdbcRepository.findById(savedEntity.getId());

        assertThat(retrievedEntity)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(originalEntity);

        assertThat(retrievedEntity.get().asMember().firstName())
                .isEqualTo("firstName");

        assertThat(savedEntity.getId())
                .isNotNull();

        assertThat(retrievedEntity.get().getId())
                .isNotNull();
    }

}