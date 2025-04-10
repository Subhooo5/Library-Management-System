import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Book {
    private int id;
    private String title;
    private String author;
    private boolean isBorrowed;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isBorrowed = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void borrowBook() {
        this.isBorrowed = true;
    }

    public void returnBook() {
        this.isBorrowed = false;
    }

    @Override
    public String toString() {
        return "Book [ID=" + id + ", Title=" + title + ", Author=" + author + ", Borrowed=" + isBorrowed + "]";
    }
}

abstract class User {
    protected String name;
    public User(String name) {
        this.name = name;
    }
    public abstract void showMenu();
}

class Reader extends User {
    public Reader(String name) {
        super(name);
    }

    @Override
    public void showMenu() {
        System.out.println("\nWelcome " + name + " :)");
        System.out.println("1. View Available Books");
        System.out.println("2. Borrow a Book");
        System.out.println("3. Return a Book");
        System.out.println("4. Exit");
    }

    public void borrowBook(Library l, Scanner s) {
        l.viewAvailableBooks();
        System.out.print("Enter Book ID to borrow: ");
        int bookId = s.nextInt();
        try {
            l.borrowBook(bookId, name);
        } catch (BookNotAvailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public void returnBook(Library l, Scanner s) {
        System.out.print("Enter Book ID to return: ");
        int bookId = s.nextInt();
        l.returnBook(bookId, name);
    }
}

class Librarian extends User {
    public Librarian(String name) {
        super(name);
    }

    @Override
    public void showMenu() {
        System.out.println("\nWelcome " + name + " :)");
        System.out.println("1. Add a Book");
        System.out.println("2. Remove a Book");
        System.out.println("3. View Available Books");
        System.out.println("4. Exit");
    }

    public void addBook(Library l, Scanner s) {
        System.out.print("Enter Book Title: ");
        s.nextLine();
        String title = s.nextLine();
        System.out.print("Enter Author Name: ");
        String author = s.nextLine();
        l.addBook(title, author);
    }

    public void removeBook(Library l, Scanner s) {
        System.out.print("Enter Book ID to remove: ");
        int bookId = s.nextInt();
        l.removeBook(bookId);
    }
}

class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) {
        super(message);
    }
}

class Library {
    private List<Book> books;
    private Set<Integer> borrowedBooks;
    private ReentrantLock lock = new ReentrantLock();

    public Library() {
        books = loadDefaultBooks();  // Always load fresh books
        borrowedBooks = new HashSet<>();
    }

    private List<Book> loadDefaultBooks() {
        List<Book> defaultBooks = new ArrayList<>();
        String[][] famousBooks = {
            {"To Kill a Mockingbird", "Harper Lee"}, {"1984", "George Orwell"},
            {"Pride and Prejudice", "Jane Austen"}, {"The Great Gatsby", "F. Scott Fitzgerald"},
            {"Moby Dick", "Herman Melville"}, {"War and Peace", "Leo Tolstoy"},
            {"The Catcher in the Rye", "J.D. Salinger"}, {"The Lord of the Rings", "J.R.R. Tolkien"},
            {"Animal Farm", "George Orwell"}, {"The Hobbit", "J.R.R. Tolkien"},
            {"Fahrenheit 451", "Ray Bradbury"}, {"Jane Eyre", "Charlotte Bronte"},
            {"Brave New World", "Aldous Huxley"}, {"Wuthering Heights", "Emily Bronte"},
            {"Crime and Punishment", "Fyodor Dostoevsky"}, {"Great Expectations", "Charles Dickens"},
            {"The Odyssey", "Homer"}, {"Les Misérables", "Victor Hugo"},
            {"Anna Karenina", "Leo Tolstoy"}, {"Ulysses", "James Joyce"},
            {"Don Quixote", "Miguel de Cervantes"}, {"The Iliad", "Homer"},
            {"Dracula", "Bram Stoker"}, {"The Count of Monte Cristo", "Alexandre Dumas"},
            {"A Tale of Two Cities", "Charles Dickens"}, {"The Picture of Dorian Gray", "Oscar Wilde"},
            {"Frankenstein", "Mary Shelley"}, {"The Grapes of Wrath", "John Steinbeck"},
            {"The Alchemist", "Paulo Coelho"}, {"Catch-22", "Joseph Heller"},
            {"One Hundred Years of Solitude", "Gabriel Garcia Marquez"}, {"Beloved", "Toni Morrison"},
            {"The Brothers Karamazov", "Fyodor Dostoevsky"}, {"Lolita", "Vladimir Nabokov"},
            {"Slaughterhouse-Five", "Kurt Vonnegut"}, {"David Copperfield", "Charles Dickens"},
            {"The Secret Garden", "Frances Hodgson Burnett"}, {"The Little Prince", "Antoine de Saint-Exupéry"},
            {"Alice's Adventures in Wonderland", "Lewis Carroll"}, {"The Divine Comedy", "Dante Alighieri"},
            {"The Road", "Cormac McCarthy"}, {"Gulliver's Travels", "Jonathan Swift"},
            {"The Time Machine", "H.G. Wells"}, {"The Stranger", "Albert Camus"},
            {"The Sun Also Rises", "Ernest Hemingway"}, {"A Clockwork Orange", "Anthony Burgess"},
            {"Madame Bovary", "Gustave Flaubert"}, {"Of Mice and Men", "John Steinbeck"},
            {"Treasure Island", "Robert Louis Stevenson"}, {"Dune", "Frank Herbert"}
        };
        for (int i = 0; i < famousBooks.length; i++) {
            defaultBooks.add(new Book(i + 1, famousBooks[i][0], famousBooks[i][1]));
        }
        return defaultBooks;
    }

    public synchronized void addBook(String title, String author) {
        int id = books.size() + 1;
        Book newBook = new Book(id, title, author);
        books.add(newBook);
        System.out.println("Book added successfully! :) -> " + newBook);
    }

    public synchronized void removeBook(int bookId) {
        books.removeIf(book -> book.getId() == bookId);
        borrowedBooks.remove(bookId);
        System.out.println("Book removed successfully! :)");
    }

    public void viewAvailableBooks() {
        System.out.println("Available Books:");
        for (Book book : books) {
            if (!book.isBorrowed()) {
                System.out.println(book.getId() + ". " + book.getTitle());
            }
        }
    }

    public void borrowBook(int bookId, String userName) throws BookNotAvailableException {
        lock.lock();
        try {
            for (Book book : books) {
                if (book.getId() == bookId && !book.isBorrowed()) {
                    book.borrowBook();
                    borrowedBooks.add(bookId);
                    System.out.println(userName + " borrowed " + book.getTitle());
                    return;
                }
            }
            throw new BookNotAvailableException("Book with ID " + bookId + " is not available! :(");
        } finally {
            lock.unlock();
        }
    }

    public void returnBook(int bookId, String userName) {
        lock.lock();
        try {
            for (Book book : books) {
                if (book.getId() == bookId && book.isBorrowed()) {
                    book.returnBook();
                    borrowedBooks.remove(bookId);
                    System.out.println(userName + " returned " + book.getTitle());
                    return;
                }
            }
            System.out.println("Invalid return. Book not found or not borrowed!");
        } finally {
            lock.unlock();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Library l = new Library();
        Scanner s = new Scanner(System.in);
        System.out.println("\n******  WELCOME TO OUR LIBRARY :)  ******\n");
        System.out.print("Enter your name: ");
        String name = s.nextLine();
        System.out.print("Are you a 1. Reader or 2. Librarian? (1/2): ");
        int c = s.nextInt();
        if (c == 1) {
            Reader re = new Reader(name);
            handleReaderActions(l, re, s);
        } else if (c == 2) {
            Librarian li = new Librarian(name);
            handleLibrarianActions(l, li, s);
        } else {
            System.out.println("INVALID CHOICE!! :(");
        }
        s.close();
    }

    private static void handleReaderActions(Library l, Reader re, Scanner s) {
        while (true) {
            re.showMenu();
            System.out.print("Enter your choice: ");
            int c = s.nextInt();
            switch (c) {
                case 1 -> l.viewAvailableBooks();
                case 2 -> re.borrowBook(l, s);
                case 3 -> re.returnBook(l, s);
                case 4 -> {
                    System.out.println("\nHAVE A GOOD DAY! :)");
                    return;
                }
                default -> System.out.println("INVALID CHOICE!! :(");
            }
        }
    }

    private static void handleLibrarianActions(Library l, Librarian li, Scanner s) {
        while (true) {
            li.showMenu();
            System.out.print("Enter your choice: ");
            int c = s.nextInt();
            switch (c) {
                case 1 -> li.addBook(l, s);
                case 2 -> li.removeBook(l, s);
                case 3 -> l.viewAvailableBooks();
                case 4 -> {
                    System.out.println("\nHAVE A GOOD DAY! :)");
                    return;
                }
                default -> System.out.println("INVALID CHOICE!! :(");
            }
        }
    }
}
