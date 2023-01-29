package com.luiz.libraryapi.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.luiz.libraryapi.model.entity.Book;
import com.luiz.libraryapi.model.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan , Long>{
	
	@Query (value = "select case when (count (l.id) > 0) then true else false end from Loan l where l.book = :book and (l.returned is null or  l.returned is false)")
	boolean existsByBookAndNotReturned(@Param ("book")Book book);

	Page<Loan> findByBookIsbnOrCustomer(String isbn, String customer, Pageable pageRequest);

}
