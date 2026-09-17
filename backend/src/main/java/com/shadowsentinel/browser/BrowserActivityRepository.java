package com.shadowsentinel.browser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BrowserActivityRepository extends JpaRepository<BrowserActivity, Long>, JpaSpecificationExecutor<BrowserActivity> {
}
