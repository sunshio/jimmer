package org.babyfish.jimmer.sql.example.business;

import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.spring.model.SortUtils;
import org.babyfish.jimmer.sql.example.repository.BookRepository;
import org.babyfish.jimmer.sql.example.model.*;
import org.babyfish.jimmer.sql.example.model.input.BookInput;
import org.babyfish.jimmer.sql.example.model.input.CompositeBookInput;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.runtime.SaveErrorCode;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A real project should be a three-tier architecture consisting
 * of repository, service, and controller.
 *
 * This demo has no business logic, its purpose is only to tell users
 * how to use jimmer with the <b>least</b> code. Therefore, this demo
 * does not follow this convention, and let services be directly
 * decorated by `@RestController`, not `@Service`.
 */
@RestController
@RequestMapping("/book")
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") Book> findSimpleBooks() {
        return bookRepository.findAll(SIMPLE_FETCHER, BookProps.NAME, BookProps.EDITION.desc());
    }

    @GetMapping("/list")
    public Page<@FetchBy("DEFAULT_FETCHER") Book> findBooks(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize,
            // The `sortCode` also support implicit join, like `store.name asc`
            @RequestParam(defaultValue = "name asc, edition desc") String sortCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String authorName
    ) {
        return bookRepository.findBooks(
                PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),
                name,
                storeName,
                authorName,
                DEFAULT_FETCHER
        );
    }

    @GetMapping("/{id}")
    @Nullable
    public @FetchBy("COMPLEX_FETCHER") Book findComplexBook(
            @PathVariable("id") long id
    ) {
        return bookRepository.findNullable(id, COMPLEX_FETCHER);
    }

    private static final Fetcher<Book> SIMPLE_FETCHER =
            BookFetcher.$.name().edition();

    private static final Fetcher<Book> DEFAULT_FETCHER =
            BookFetcher.$
                    .allScalarFields()
                    .tenant(false)
                    .store(
                            BookStoreFetcher.$
                                    .name()
                    )
                    .authors(
                            AuthorFetcher.$
                                    .firstName()
                                    .lastName()
                    );

    private static final Fetcher<Book> COMPLEX_FETCHER =
            BookFetcher.$
                    .allScalarFields()
                    .tenant(false)
                    .store(
                            BookStoreFetcher.$
                                    .allScalarFields()
                                    .avgPrice()
                    )
                    .authors(
                            AuthorFetcher.$
                                    .allScalarFields()
                    );

    @PutMapping
    @ThrowsAll(SaveErrorCode.class)
    public Book saveBook(@RequestBody BookInput input) {
        return bookRepository.save(input);
    }

    @PutMapping("/composite")
    @ThrowsAll(SaveErrorCode.class)
    public Book saveCompositeBook(@RequestBody CompositeBookInput input) {
        return bookRepository.save(input);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable("id") long id) {
        bookRepository.deleteById(id);
    }
}
