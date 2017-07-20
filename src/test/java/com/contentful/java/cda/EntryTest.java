package com.contentful.java.cda;

import com.contentful.java.cda.lib.Enqueue;
import com.contentful.java.cda.lib.TestCallback;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

import static com.google.common.truth.Truth.assertThat;

public class EntryTest extends BaseTest {
  @Test(expected = CDAResourceNotFoundException.class)
  @Enqueue("array_empty.json")
  public void fetchNonExistingReturnsNull() throws Exception {
    try {
      client.fetch(CDAEntry.class).one("foo");
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).isEqualTo("Could not find id 'foo' of type 'CDAEntry'.");
      throw e;
    }
  }

  @Test
  @Enqueue("array_empty.json")
  public void fetchNonExistingEntryThrowsError() throws Exception {
    final Object result[] = new Object[]{new Object()};
    final Object error[] = new Object[]{new Object()};
    final CountDownLatch latch = new CountDownLatch(1);

    client.observe(CDAEntry.class)
        .one("foo")
        .subscribe(
            new Consumer<CDAEntry>() {
              @Override public void accept(CDAEntry entry) {
                result[0] = entry;
                error[0] = null;

                latch.countDown();
              }
            }, new Consumer<Throwable>() {
              @Override public void accept(Throwable throwable) {
                result[0] = null;
                error[0] = throwable;

                latch.countDown();
              }
            }
        );

    latch.await(1, TimeUnit.SECONDS);

    assertThat(result[0]).isNull();
    assertThat(error[0]).isNotNull();
    assertThat(error[0]).isInstanceOf(CDAResourceNotFoundException.class);
    final CDAResourceNotFoundException notFoundException = (CDAResourceNotFoundException) error[0];
    final String message = notFoundException.getMessage();
    assertThat(message).isEqualTo("Could not find id 'foo' of type 'CDAEntry'.");
  }

  @Test
  @Enqueue("array_empty.json")
  public void fetchNonExistingEntryInvokesSuccessWithNull() throws Exception {
    TestCallback<CDAEntry> callback = client.fetch(CDAEntry.class)
        .one("foo", new TestCallback<CDAEntry>())
        .await();

    assertThat(callback.error()).isNotNull();
    final String message = callback.error().getMessage();
    assertThat(message).isEqualTo("Could not find id 'foo' of type 'CDAEntry'.");

    assertThat(callback.result()).isNull();
  }

  @Test
  @Enqueue("demo/entries_nofields.json")
  public void entryNoFields() throws Exception {
    CDAEntry foo = client.fetch(CDAEntry.class).one("foo");
    assertThat(foo).isNotNull();
    assertThat(foo.fields).isEmpty();
  }

  @Test
  @Enqueue("demo/entries_nyancat.json")
  public void entryContentType() throws Exception {
    CDAEntry entry = client.fetch(CDAEntry.class).one("nyancat");
    assertThat(entry.contentType()).isNotNull();
  }

  @Test
  @Enqueue("demo/entries_nyancat.json")
  public void fetchEntry() throws Exception {
    assertNyanCat(client.fetch(CDAEntry.class).one("nyancat"));
  }

  @Test
  @Enqueue("demo/entries_nyancat.json")
  public void fetchEntryAsync() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final CDAEntry[] result = {null};

    client.fetch(CDAEntry.class).one("nyancat", new CDACallback<CDAEntry>() {
      @Override protected void onSuccess(CDAEntry entry) {
        result[0] = entry;
        latch.countDown();
      }

      @Override protected void onFailure(Throwable error) {
        latch.countDown();
      }
    });

    latch.await(1, TimeUnit.SECONDS);
    assertThat(result[0]).isNotNull();
    assertNyanCat(result[0]);
  }

  @Test
  @Enqueue("demo/entries.json")
  public void fetchAllEntries() throws Exception {
    CDAArray array = client.fetch(CDAEntry.class).all();
    assertThat(array.items()).hasSize(11);
    assertThat(array.assets()).hasSize(4);
    assertThat(array.entries()).hasSize(11);

    assertThat(array.total()).isEqualTo(11);
    assertThat(array.skip()).isEqualTo(0);
    assertThat(array.limit()).isEqualTo(100);

    for (CDAEntry entry : array.entries().values()) {
      assertThat(entry.contentType()).isNotNull();
    }

    CDAEntry nyanCat = array.entries().get("nyancat");
    assertThat(nyanCat).isNotNull();
    assertNyanCat(nyanCat);
  }

  private void assertNyanCat(CDAEntry entry) {
    assertThat(entry.id()).isEqualTo("nyancat");
    assertThat(entry.getField("name")).isEqualTo("Nyan Cat");
    assertThat(entry.getField("color")).isEqualTo("rainbow");
    assertThat(entry.getField("birthday")).isEqualTo("2011-04-04T22:00:00+00:00");
    assertThat(entry.getField("lives")).isEqualTo(1337.0);

    List<String> likes = entry.getField("likes");
    assertThat(likes).containsExactly("rainbows", "fish");

    Object bestFriend = entry.getField("bestFriend");
    assertThat(bestFriend).isInstanceOf(CDAEntry.class);
    assertThat(entry).isSameAs(((CDAEntry) bestFriend).getField("bestFriend"));

    // Localization
    assertThat(entry.locale()).isEqualTo("en-US");
    assertThat(entry.getField("color")).isEqualTo("rainbow");
    assertThat(entry.getField("non-existing-does-not-throw")).isNull();
  }

  @Test
  @Enqueue(
      defaults = {"arrays/space.json", "arrays/content_types.json"},
      value = "arrays/entries.json"
  )
  public void arrayItemsContainOnlyTopLevelEntries() throws Exception {
    CDAArray array =
        client.fetch(CDAEntry.class).where("content_type", "Jm9AuzgH8OyocaMQSMwKC").all();

    assertThat(array.items()).hasSize(1);
    assertThat(array.entries()).hasSize(2);
    assertThat(array.entries().values()).containsAllIn(array.items());

    CDAEntry parent = array.entries().get("7Avw18DWveMI60a0WWwyCi");
    assertThat(parent).isNotNull();

    CDAEntry child = parent.getField("child");
    assertThat(child).isNotNull();

    assertThat(array.items()).containsExactly(parent);
    assertThat(array.entries().values()).containsExactly(parent, child);
  }
}
