package io.myplant.repository;

import io.myplant.domain.OverwriteState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OverwriteStateRepository extends JpaRepository<OverwriteState, Long> {
    List<OverwriteState> findByAssetIdIn(Set<Long> assetIds);

    List<OverwriteState> findByAssetId(Long assetId);
}
