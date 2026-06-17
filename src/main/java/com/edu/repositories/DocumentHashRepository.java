package com.edu.repositories;

import com.edu.entity.DocumentHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentHashRepository extends JpaRepository<DocumentHash, UUID> {

    @Query("SELECT dh FROM DocumentHash dh WHERE dh.chunk0 = :c0 OR dh.chunk1 = :c1 OR dh.chunk2 = :c2 OR dh.chunk3 = :c3 OR dh.chunk4 = :c4 OR dh.chunk5 = :c5 OR dh.chunk6 = :c6 OR dh.chunk7 = :c7")
    List<DocumentHash> findCandidatesByChunks(
        @Param("c0") String c0,
        @Param("c1") String c1,
        @Param("c2") String c2,
        @Param("c3") String c3,
        @Param("c4") String c4,
        @Param("c5") String c5,
        @Param("c6") String c6,
        @Param("c7") String c7
    );
}
