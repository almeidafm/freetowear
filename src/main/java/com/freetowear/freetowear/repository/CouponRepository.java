package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
}