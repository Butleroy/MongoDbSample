package io.myalfred.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.myalfred.mongodb.data.TestAuthor;
import io.myalfred.mongodb.data.TestBook;
import io.myalfred.mongodb.data.TestBookStore;
import io.myalfred.mongodb.databases.TestAuthorDatabase;
import io.myalfred.mongodb.databases.TestBookDatabase;
import io.myalfred.mongodb.databases.TestBookStoreDatabase;

public class BookStoreDatabaseTest {

	private TestBookDatabase bookDb = new TestBookDatabase();
	private TestAuthorDatabase authorDb = new TestAuthorDatabase();
	private TestBookStoreDatabase bookStoreDb = new TestBookStoreDatabase();

	@Test
	public void testDatabaseStoreLoad() {

		bookDb.removeAll();
		authorDb.removeAll();

		TestAuthor author1 = new TestAuthor("testFirstName", "testLastName");
		assertTrue(authorDb.store(author1));
		assertEquals(1, authorDb.objectCount());

		TestBook book1 = new TestBook(author1, 10.0);

		assertTrue(bookDb.store(book1));
		assertEquals(1, bookDb.objectCount());

		book1 = bookDb.load(book1.getId());

		assertTrue(book1.getAuthor() != null);
		assertEquals("testFirstName", book1.getAuthor().getFirstName());
		assertEquals("testLastName", book1.getAuthor().getLastName());

		assertTrue(book1.getId() != null);
		assertTrue(author1.getId() != null);

		assertTrue(bookDb.delete(book1));
		assertTrue(authorDb.delete(author1));

		int objectCount = 1000;

		List<TestBook> bookList = new ArrayList<>();
		List<TestAuthor> authorList = new ArrayList<>();

		for (int i = 0; i < objectCount; i++) {

			TestAuthor author = new TestAuthor("first" + i, "last" + i);
			authorList.add(author);
			bookList.add(new TestBook(author, 1.0, 2.0));
		}

		assertTrue(authorDb.storeAll(authorList));
		assertTrue(bookDb.storeAll(bookList));
		assertEquals(objectCount, authorDb.objectCount());
		assertEquals(objectCount, bookDb.objectCount());

		long start = System.currentTimeMillis();

		List<TestBook> loaded = bookDb.loadAll();

		assertEquals(objectCount, loaded.size());
		assertTrue(System.currentTimeMillis() - start < 5000);

		start = System.currentTimeMillis();

		TestBook data = bookDb.findLastName("last500");

		assertEquals("first500", data.getAuthor().getFirstName());
		assertEquals("last500", data.getAuthor().getLastName());

		start = System.currentTimeMillis();

		data = bookDb.findLastName("last900");

		assertEquals("first900", data.getAuthor().getFirstName());
		assertEquals("last900", data.getAuthor().getLastName());

		bookDb.removeAll();
		assertEquals(0, bookDb.objectCount());
		authorDb.removeAll();
		assertEquals(0, authorDb.objectCount());
	}

	@Test
	public void testDatabaseDelete() {

		bookDb.removeAll();

		TestAuthor a1 = new TestAuthor("first1", "last1");
		TestAuthor a2 = new TestAuthor("first2", "last2");

		TestBook b1 = new TestBook(a1, 1.0, 2.0);
		TestBook b2 = new TestBook(a2, 3.0, 4.0);
		TestBook b3 = new TestBook(a1, 5.0, 6.0);

		List<TestBook> list = new ArrayList<>();
		list.add(b1);
		list.add(b2);
		list.add(b3);

		assertEquals(0, bookDb.objectCount());
		assertEquals(null, b1.getId());

		assertTrue(authorDb.store(a1));
		assertTrue(authorDb.store(a2));
		assertTrue(bookDb.storeAll(list));

		TestBook loaded = bookDb.load(b1.getId());
		assertTrue(loaded != null);

		assertEquals(3, bookDb.objectCount());
		assertFalse(b1.getId() == null);

		assertTrue(bookDb.delete(b2.getId()));
		assertEquals(2, bookDb.objectCount());

		assertTrue(bookDb.delete(b3.getId()));
		assertEquals(1, bookDb.objectCount());

		assertTrue(bookDb.delete(b1.getId()));
		assertEquals(0, bookDb.objectCount());
	}

	@Test
	public void testDatabaseUpdate() {

		bookDb.removeAll();
		authorDb.removeAll();

		TestAuthor a1 = new TestAuthor("FirstName", "LastName");
		TestBook b1 = new TestBook(a1, 1.0, 2.0);

		assertFalse(authorDb.update(a1));
		assertFalse(bookDb.update(b1));

		assertEquals(0, authorDb.objectCount());
		assertTrue(authorDb.store(a1));
		assertEquals(1, authorDb.objectCount());

		assertEquals(0, bookDb.objectCount());
		assertTrue(bookDb.store(b1));
		assertEquals(1, bookDb.objectCount());

		assertEquals("FirstName", bookDb.load(b1.getId()).getAuthor().getFirstName());
		assertEquals("LastName", bookDb.load(b1.getId()).getAuthor().getLastName());
		assertEquals(1.0, bookDb.load(b1.getId()).getPrices().get(0), 0.01);

		List<Double> prices = new ArrayList<>();
		prices.add(3.0);
		prices.add(4.0);
		b1.setPrices(prices);
		assertTrue(bookDb.update(b1));

		assertEquals(3.0, bookDb.load(b1.getId()).getPrices().get(0), 0.01);

		a1.setLastName("New LastName");
		assertTrue(authorDb.update(a1));
		assertEquals(1, authorDb.objectCount());
		assertEquals("New LastName", bookDb.load(b1.getId()).getAuthor().getLastName());

		assertEquals("FirstName", bookDb.load(b1.getId()).getAuthor().getFirstName());
		assertTrue(authorDb.update(a1.getId(), "firstName", "New FirstName"));
		assertEquals("New FirstName", bookDb.load(b1.getId()).getAuthor().getFirstName());
		assertEquals(1, bookDb.objectCount());

		assertTrue(bookDb.delete(b1.getId()));
		assertEquals(0, bookDb.objectCount());

		assertTrue(authorDb.delete(a1.getId()));
		assertEquals(0, authorDb.objectCount());
	}

	@Test
	public void testBookStore() {

		bookStoreDb.removeAll();
		authorDb.removeAll();
		bookDb.removeAll();

		TestAuthor a1 = new TestAuthor("Dan", "Brown");
		TestAuthor a2 = new TestAuthor("Stephen", "King");

		assertTrue(authorDb.store(a1));
		assertTrue(authorDb.store(a2));
		assertEquals(2, authorDb.objectCount());

		int bookCount = 6;
		List<TestBook> books = new ArrayList<>();

		for (int i = 0; i < bookCount; i++) {

			// switch between authors
			TestBook book = new TestBook(i % 2 == 0 ? a1 : a2, 10.0 + i);

			assertTrue(bookDb.store(book));
			books.add(book);
		}

		assertEquals(6, bookDb.objectCount());

		TestBookStore store = new TestBookStore();
		store.setName("Thalia");
		store.setBooks(books);

		assertTrue(bookStoreDb.store(store));
		assertEquals(1, bookStoreDb.objectCount());

		TestBookStore loaded = bookStoreDb.load(store.getId());
		assertTrue(loaded != null);

		assertEquals("Thalia", loaded.getName());
		assertEquals(6, loaded.getBooks().size());

		assertEquals("Dan", loaded.getBooks().get(0).getAuthor().getFirstName());
		assertEquals("Stephen", loaded.getBooks().get(1).getAuthor().getFirstName());

		assertEquals(a1.getId(), loaded.getBooks().get(0).getAuthor().getId());
		assertEquals(a2.getId(), loaded.getBooks().get(1).getAuthor().getId());

		assertEquals(10.0, loaded.getBooks().get(0).getPrices().get(0), 0.01);
		assertEquals(15.0, loaded.getBooks().get(5).getPrices().get(0), 0.01);
	}

	@Test
	public void testUpsert() {

		authorDb.removeAll();

		assertFalse(authorDb.upsert(null));
		assertEquals(0, authorDb.objectCount());

		TestAuthor a1 = new TestAuthor("Dan", "Brown");

		assertTrue(authorDb.upsert(a1));

		TestAuthor a2 = new TestAuthor("Stephen", "King");
		assertTrue(authorDb.upsert(a2));

		assertEquals(2, authorDb.objectCount());

		a1 = authorDb.load(a1.getId());
		assertEquals("Dan", a1.getFirstName());

		a1.setFirstName("Dan1");
		assertTrue(authorDb.upsert(a1));

		TestAuthor a1Updated = authorDb.load(a1.getId());
		assertEquals("Dan1", a1Updated.getFirstName());
		assertEquals(a1.getId(), a1Updated.getId());
		assertEquals(2, authorDb.objectCount());

		a2 = authorDb.load(a2.getId());
		assertEquals("Stephen", a2.getFirstName());

		a2.setFirstName("Stephen1");
		assertTrue(authorDb.upsert(a2));

		TestAuthor a2Updated = authorDb.load(a2.getId());
		assertEquals("Stephen1", a2Updated.getFirstName());
		assertEquals(a2.getId(), a2Updated.getId());
		assertEquals(2, authorDb.objectCount());

		authorDb.removeAll();
		assertEquals(0, authorDb.objectCount());
	}
}
