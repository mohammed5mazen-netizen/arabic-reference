package com.mrsoft.arabicreference.search.domain;

import java.util.List;

/** Published snapshots that may appear in the public search index. */
public interface PublishedSearchSource {

    List<SearchDocument> publishedDocuments();
}
