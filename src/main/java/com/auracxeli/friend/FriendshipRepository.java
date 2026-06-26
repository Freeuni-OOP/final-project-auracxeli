package com.auracxeli.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
            select f from Friendship f
            where (f.requester.id = :userA and f.addressee.id = :userB)
               or (f.requester.id = :userB and f.addressee.id = :userA)
            """)
    Optional<Friendship> findBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("""
            select f from Friendship f
            where f.status = :status
              and (f.requester.id = :userId or f.addressee.id = :userId)
            """)
    List<Friendship> findByStatusForUser(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);
}
