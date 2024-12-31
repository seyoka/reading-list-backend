package com.example.demo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/currently-reading")
    public Book getCurrentlyReadingBook() {
        return bookRepository.findAll().stream()
                .filter(Book::isCurrentlyReading)
                .findFirst()
                .orElse(null);
    }
    
    @GetMapping("/book-list")
    public List<Book> getBookList() {
        return bookRepository.findAll().stream()
                .filter(book -> !book.isCurrentlyReading() && book.getCompletionDate() == null)
                .collect(Collectors.toList());
    }


    @PostMapping
    public Book addBook(@RequestBody Book book) {
        book.setCurrentlyReading(false); // Default new books to not currently reading
        return bookRepository.save(book);
    }

    @PutMapping("/{id}/mark-currently-reading")
    public Book markAsCurrentlyReading(@PathVariable String id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            book.setCurrentlyReading(true);
            book.setCompleted(false);
            book.setStartDate(LocalDate.now()); // Set start date
            book.setCompletionDate(null); // Clear any previous completion date
            return bookRepository.save(book);
        }
        return null;
    }


    @PutMapping("/{id}")
    public Book updateBookProgress(@PathVariable String id, @RequestBody Book updatedBook) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            int newPageCount = book.getPagesRead() + updatedBook.getPagesRead();
    
            
            if (newPageCount < 0) {
                throw new IllegalArgumentException("Pages read cannot be negative.");
            }
            if (newPageCount > book.getTotalPages()) {
                throw new IllegalArgumentException("Pages read cannot exceed total pages.");
            }
    
            book.setPagesRead(newPageCount);
            return bookRepository.save(book);
        }
        return null;
    }

    @PutMapping("/{id}/mark-completed")
    public Book markAsCompleted(@PathVariable String id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            book.setCompleted(true);
            book.setCurrentlyReading(false);
            book.setCompletionDate(LocalDate.now()); // Set completion date
            return bookRepository.save(book);
        }
        return null;
    }
    
    @GetMapping("/completed")
    public List<Book> getCompletedBooks() {
        return bookRepository.findAll().stream()
                .filter(Book::isCompleted)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        System.out.println("Delete request received for book ID: " + id);
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            System.out.println("Book deleted successfully.");
            return ResponseEntity.noContent().build();
        } else {
            System.out.println("Book with ID " + id + " not found.");
            return ResponseEntity.notFound().build();
        }
    }
    

    
}
