package com.luiz.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.luiz.libraryapi.model.entity.Book;

public interface BookService {

	Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find( Book filter, Pageable pageRequest );

    Optional<Book> getBookByIsbn(String isbn);
}
