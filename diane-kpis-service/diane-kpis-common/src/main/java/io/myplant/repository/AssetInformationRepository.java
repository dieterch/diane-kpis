package io.myplant.repository;

import io.myplant.domain.AssetInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetInformationRepository extends JpaRepository<AssetInformation, Long> {

}
