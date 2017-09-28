package com.contentful.java.cda;

import com.contentful.java.cda.lib.Enqueue;

import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class LinkTest extends BaseTest {
  @Test @Enqueue("demo/links_unresolved.json") public void unresolvedLinkIsNull() throws Exception {
    CDAArray array = client.fetch(CDAEntry.class).all();
    assertThat(array.items()).hasSize(1);
    assertThat(array.entries()).hasSize(1);

    CDAEntry entry = array.entries().get("happycat");
    assertThat(entry).isNotNull();
    assertThat(entry.getField("bestFriend")).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test @Enqueue(defaults = {
      "links/space.json",
      "links/content_types.json"
  }, value = {
      "links/entries.json"
  }) public void arrays() throws Exception {
    CDAArray array = client.fetch(CDAEntry.class).all();
    assertThat(array.total()).isEqualTo(4);
    assertThat(array.items()).hasSize(4);
    assertThat(array.assets()).hasSize(2);
    assertThat(array.entries()).hasSize(4);

    CDAEntry container = array.entries().get("3vyEoAvlkk8yE4a8gCCkiu");
    assertThat(container).isNotNull();
    assertThat(container.getField("asset")).isInstanceOf(CDAAsset.class);
    assertThat(container.getField("entry")).isInstanceOf(CDAEntry.class);

    List<CDAAsset> assets = container.getField("assets");
    assertThat(assets).isNotNull();
    assertThat(assets).hasSize(2);
    assertThat(assets.get(0).id()).isEqualTo("3xkzMDqRTaoIeKkUYwiIUw");
    assertThat(assets.get(1).id()).isEqualTo("5WHMX35TkQg08sK0agoMw");

    List<CDAEntry> entries = container.getField("entries");
    assertThat(entries).isNotNull();
    assertThat(entries).hasSize(3);
    assertThat(entries.get(0).id()).isEqualTo("4NvEw8RaUUkSa2uEEogAeG");
    assertThat(entries.get(1).id()).isEqualTo("3XNpMBumdOsWuYUs0wsgMS");
    assertThat(entries.get(2).id()).isEqualTo("4kDCK9U7OgQiieIqi6ScWA");

    assertThat((List<String>) container.getField("symbols")).containsExactly("a", "b", "c");
  }

  @Test @Enqueue(defaults = {
      "links/space.json",
      "links/content_types.json"
  }, value = {
      "links/sync_empty_links.json"
  }) public void testEmptyLinks() throws Exception {
    client.sync().fetch();
  }

  @SuppressWarnings("unchecked")
  @Test @Enqueue(defaults = {
      "links/space.json",
      "links/content_types.json"
  }, value = {
      "errors/include_non_public_entries.json"
  }) public void includeNonPublishedEntriesDoNotSurface() throws Exception {
    final CDAArray array = client.fetch(CDAEntry.class).all();
    assertThat(array.items().size()).isEqualTo(1);

    final CDAEntry entry = (CDAEntry) array.items().get(0);
    assertThat(entry.contentType().id()).isEqualTo("2s9novBkP2G0oasUaG8kCs");
    assertThat(entry.rawFields().size()).isEqualTo(1);

    final Object entries = entry.getField("entries");
    assertThat(entries).isInstanceOf(List.class);
    assertThat(((List) entries).get(0)).isInstanceOf(CDAEntry.class);

    assertThat(array.errors().size()).isEqualTo(1);

    final CDAArrayError error = array.errors().get(0);
    assertThat(error.getDetails().getType()).isEqualTo("Link");
    assertThat(error.getDetails().getLinkType()).isEqualTo("Entry");
    assertThat(error.getDetails().getId()).isEqualTo("4iO7CN7tSE8aia4wm2CkmS");
  }
}
