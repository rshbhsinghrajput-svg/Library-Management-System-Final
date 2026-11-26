package com.library;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@SpringBootApplication
public class LibraryApplication {
 public static void main(String[] args){SpringApplication.run(LibraryApplication.class,args);}
 @Bean WebMvcConfigurer cors(){return new WebMvcConfigurer(){public void addCorsMappings(CorsRegistry r){r.addMapping("/api/**").allowedOrigins("*").allowedMethods("*");}};}
}

@Entity @Table(name="books")
class Book {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @NotBlank String title; @NotBlank String author; @NotBlank String category; String isbn;
 int quantity; int availableCopies;
 public Book(){} public Book(String t,String a,String c,String i,int q){title=t;author=a;category=c;isbn=i;quantity=q;availableCopies=q;}
 public Long getId(){return id;} public String getTitle(){return title;} public void setTitle(String v){title=v;}
 public String getAuthor(){return author;} public void setAuthor(String v){author=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public String getIsbn(){return isbn;} public void setIsbn(String v){isbn=v;}
 public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
 public int getAvailableCopies(){return availableCopies;} public void setAvailableCopies(int v){availableCopies=v;}
}
@Entity @Table(name="students")
class Student {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @NotBlank String name; @NotBlank @Column(unique=true) String email; String phone; String course; String password;
 public Student(){} public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getCourse(){return course;} public void setCourse(String v){course=v;} public String getPassword(){return password;} public void setPassword(String v){password=v;}
}
@Entity @Table(name="issue_records")
class IssueRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(optional=false) Book book; @ManyToOne(optional=false) Student student;
 LocalDate issueDate,dueDate,returnDate; double fine;
 public IssueRecord(){} public IssueRecord(Book b,Student s,int days){book=b;student=s;issueDate=LocalDate.now();dueDate=issueDate.plusDays(days);}
 public Long getId(){return id;} public Book getBook(){return book;} public Student getStudent(){return student;}
 public LocalDate getIssueDate(){return issueDate;} public LocalDate getDueDate(){return dueDate;} public LocalDate getReturnDate(){return returnDate;}
 public double getFine(){return fine;} public void setReturnDate(LocalDate v){returnDate=v;} public void setFine(double v){fine=v;}
}
interface BookRepo extends org.springframework.data.jpa.repository.JpaRepository<Book,Long>{List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String t,String a);}
interface StudentRepo extends org.springframework.data.jpa.repository.JpaRepository<Student,Long>{}
interface IssueRepo extends org.springframework.data.jpa.repository.JpaRepository<IssueRecord,Long>{List<IssueRecord> findByReturnDateIsNull();}

@RestController @RequestMapping("/api/books")
class BookApi {
 private final BookRepo r; BookApi(BookRepo r){this.r=r;}
 @GetMapping List<Book> all(@RequestParam(required=false) String q){return q==null||q.isBlank()?r.findAll():r.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q,q);}
 @PostMapping Book add(@RequestBody Book b){b.setAvailableCopies(b.getQuantity());return r.save(b);}
 @DeleteMapping("/{id}") ResponseEntity<?> del(@PathVariable Long id){r.deleteById(id);return ResponseEntity.ok().build();}
}
@RestController @RequestMapping("/api/students")
class StudentApi {
 private final StudentRepo r; StudentApi(StudentRepo r){this.r=r;}
 @GetMapping List<Student> all(){return r.findAll();}
 @PostMapping Student add(@RequestBody Student s){return r.save(s);}
}
@RestController @RequestMapping("/api/issues")
class IssueApi {
 private final IssueRepo ir; private final BookRepo br; private final StudentRepo sr;
 IssueApi(IssueRepo i,BookRepo b,StudentRepo s){ir=i;br=b;sr=s;}
 @GetMapping List<IssueRecord> all(){return ir.findAll();}
 @PostMapping IssueRecord issue(@RequestBody IssueRequest x){
  Book b=br.findById(x.bookId()).orElseThrow(); Student s=sr.findById(x.studentId()).orElseThrow();
  if(b.getAvailableCopies()<=0) throw new IllegalStateException("Book unavailable");
  b.setAvailableCopies(b.getAvailableCopies()-1); br.save(b); return ir.save(new IssueRecord(b,s,x.days()));
 }
 @PostMapping("/{id}/return") IssueRecord ret(@PathVariable Long id){
  IssueRecord x=ir.findById(id).orElseThrow(); if(x.getReturnDate()==null){LocalDate d=LocalDate.now();x.setReturnDate(d);
   x.setFine(Math.max(0,ChronoUnit.DAYS.between(x.getDueDate(),d))*5.0);Book b=x.getBook();b.setAvailableCopies(b.getAvailableCopies()+1);br.save(b);ir.save(x);} return x;
 }
 record IssueRequest(Long bookId,Long studentId,int days){}
}
@RestController @RequestMapping("/api/dashboard")
class DashboardApi {
 private final BookRepo b; private final StudentRepo s; private final IssueRepo i;
 DashboardApi(BookRepo b,StudentRepo s,IssueRepo i){this.b=b;this.s=s;this.i=i;}
 @GetMapping MapData data(){return new MapData(b.count(),s.count(),i.findByReturnDateIsNull().size());}
 record MapData(long books,long students,long issued){}
}
