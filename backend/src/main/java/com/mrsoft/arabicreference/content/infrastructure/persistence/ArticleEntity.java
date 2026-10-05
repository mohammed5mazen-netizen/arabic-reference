package com.mrsoft.arabicreference.content.infrastructure.persistence;

import com.mrsoft.arabicreference.content.domain.ArticleType;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "article")
public class ArticleEntity extends EditorialEntity {

    @Column(name = "title_original", nullable = false, length = 200)
    private String titleOriginal;

    @Column(name = "title_normalized", nullable = false, length = 200)
    private String titleNormalized;

    @Column(nullable = false, length = 220)
    private String slug;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_type", nullable = false, length = 32)
    private ArticleType articleType;

    @Column(name = "cover_label", length = 160)
    private String coverLabel;

    @Column(name = "editor_name", length = 160)
    private String editorName;

    public String getTitleOriginal() { return titleOriginal; }
    public void setTitleOriginal(String titleOriginal) { this.titleOriginal = titleOriginal; }
    public String getTitleNormalized() { return titleNormalized; }
    public void setTitleNormalized(String titleNormalized) { this.titleNormalized = titleNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public ArticleType getArticleType() { return articleType; }
    public void setArticleType(ArticleType articleType) { this.articleType = articleType; }
    public String getCoverLabel() { return coverLabel; }
    public void setCoverLabel(String coverLabel) { this.coverLabel = coverLabel; }
    public String getEditorName() { return editorName; }
    public void setEditorName(String editorName) { this.editorName = editorName; }
}
