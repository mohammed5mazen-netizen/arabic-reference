package com.mrsoft.arabicreference.content.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "article_tag_link")
@IdClass(ArticleTagLinkEntity.Key.class)
public class ArticleTagLinkEntity {

    @Id
    @Column(name = "article_id")
    private UUID articleId;

    @Id
    @Column(name = "tag_id")
    private UUID tagId;

    public ArticleTagLinkEntity() {
    }

    public ArticleTagLinkEntity(UUID articleId, UUID tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }

    public UUID getArticleId() { return articleId; }
    public UUID getTagId() { return tagId; }

    public static class Key implements Serializable {
        private UUID articleId;
        private UUID tagId;

        public Key() {
        }

        public Key(UUID articleId, UUID tagId) {
            this.articleId = articleId;
            this.tagId = tagId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && articleId.equals(key.articleId) && tagId.equals(key.tagId);
        }

        @Override
        public int hashCode() {
            return articleId.hashCode() * 31 + tagId.hashCode();
        }
    }
}
