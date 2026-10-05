package com.mrsoft.arabicreference.identity.domain;

import java.util.List;
import java.util.Set;

public final class PermissionCatalog {

    public static final String USER_VIEW = "admin.user.view";
    public static final String USER_CREATE = "admin.user.create";
    public static final String USER_EDIT = "admin.user.edit";
    public static final String USER_ACTIVATE = "admin.user.activate";
    public static final String USER_DEACTIVATE = "admin.user.deactivate";
    public static final String USER_UNLOCK = "admin.user.unlock";
    public static final String USER_RESET_PASSWORD = "admin.user.reset_password";
    public static final String ROLE_VIEW = "admin.role.view";
    public static final String ROLE_CREATE = "admin.role.create";
    public static final String ROLE_EDIT = "admin.role.edit";
    public static final String ROLE_ASSIGN = "admin.role.assign";
    public static final String PERMISSION_VIEW = "admin.permission.view";
    public static final String AUDIT_VIEW = "admin.audit.view";
    public static final String CONTENT_CREATE = "editorial.content.create";
    public static final String CONTENT_EDIT = "editorial.content.edit";
    public static final String CONTENT_SUBMIT = "editorial.content.submit";
    public static final String CONTENT_REVIEW = "editorial.content.review";
    public static final String CONTENT_APPROVE = "editorial.content.approve";
    public static final String CONTENT_PUBLISH = "editorial.content.publish";
    public static final String CONTENT_ARCHIVE = "editorial.content.archive";
    public static final String SOURCE_VIEW = "source.view";
    public static final String SOURCE_MANAGE = "source.manage";
    public static final String ENTRY_VIEW = "dictionary.entry.view";
    public static final String ENTRY_CREATE = "dictionary.entry.create";
    public static final String ENTRY_EDIT = "dictionary.entry.edit";
    public static final String ENTRY_SUBMIT = "dictionary.entry.submit";
    public static final String ENTRY_REVIEW = "dictionary.entry.review";
    public static final String ENTRY_PUBLISH = "dictionary.entry.publish";
    public static final String ENTRY_ARCHIVE = "dictionary.entry.archive";
    public static final String ROOT_VIEW = "dictionary.root.view";
    public static final String ROOT_MANAGE = "dictionary.root.manage";
    public static final String SENSE_MANAGE = "dictionary.sense.manage";
    public static final String RELATION_MANAGE = "dictionary.relation.manage";
    public static final String EXAMPLE_MANAGE = "dictionary.example.manage";
    public static final String CITATION_MANAGE = "citation.manage";
    public static final String MORPHOLOGY_VIEW = "morphology.view";
    public static final String MORPHOLOGY_PATTERN_MANAGE = "morphology.pattern.manage";
    public static final String MORPHOLOGY_ANALYSIS_CREATE = "morphology.analysis.create";
    public static final String MORPHOLOGY_ANALYSIS_EDIT = "morphology.analysis.edit";
    public static final String MORPHOLOGY_ANALYSIS_REVIEW = "morphology.analysis.review";
    public static final String MORPHOLOGY_ANALYSIS_PUBLISH = "morphology.analysis.publish";
    public static final String MORPHOLOGY_RULE_VIEW = "morphology.rule.view";
    public static final String MORPHOLOGY_RULE_MANAGE = "morphology.rule.manage";
    public static final String GRAMMAR_TOPIC_VIEW = "grammar.topic.view";
    public static final String GRAMMAR_TOPIC_MANAGE = "grammar.topic.manage";
    public static final String GRAMMAR_RULE_VIEW = "grammar.rule.view";
    public static final String GRAMMAR_RULE_CREATE = "grammar.rule.create";
    public static final String GRAMMAR_RULE_EDIT = "grammar.rule.edit";
    public static final String GRAMMAR_RULE_SUBMIT = "grammar.rule.submit";
    public static final String GRAMMAR_RULE_REVIEW = "grammar.rule.review";
    public static final String GRAMMAR_RULE_PUBLISH = "grammar.rule.publish";
    public static final String GRAMMAR_RULE_ARCHIVE = "grammar.rule.archive";
    public static final String GRAMMAR_CONCEPT_VIEW = "grammar.concept.view";
    public static final String GRAMMAR_CONCEPT_MANAGE = "grammar.concept.manage";
    public static final String GRAMMAR_EXAMPLE_MANAGE = "grammar.example.manage";
    public static final String GRAMMAR_ANNOTATION_MANAGE = "grammar.annotation.manage";
    public static final String SEARCH_ADMIN_VIEW = "search.admin.view";
    public static final String SEARCH_REINDEX = "search.reindex";

    private static final List<String> ALL = List.of(
            USER_VIEW,
            USER_CREATE,
            USER_EDIT,
            USER_ACTIVATE,
            USER_DEACTIVATE,
            USER_UNLOCK,
            USER_RESET_PASSWORD,
            ROLE_VIEW,
            ROLE_CREATE,
            ROLE_EDIT,
            ROLE_ASSIGN,
            PERMISSION_VIEW,
            AUDIT_VIEW,
            CONTENT_CREATE,
            CONTENT_EDIT,
            CONTENT_SUBMIT,
            CONTENT_REVIEW,
            CONTENT_APPROVE,
            CONTENT_PUBLISH,
            CONTENT_ARCHIVE,
            SOURCE_VIEW,
            SOURCE_MANAGE,
            ENTRY_VIEW,
            ENTRY_CREATE,
            ENTRY_EDIT,
            ENTRY_SUBMIT,
            ENTRY_REVIEW,
            ENTRY_PUBLISH,
            ENTRY_ARCHIVE,
            ROOT_VIEW,
            ROOT_MANAGE,
            SENSE_MANAGE,
            RELATION_MANAGE,
            EXAMPLE_MANAGE,
            CITATION_MANAGE,
            MORPHOLOGY_VIEW,
            MORPHOLOGY_PATTERN_MANAGE,
            MORPHOLOGY_ANALYSIS_CREATE,
            MORPHOLOGY_ANALYSIS_EDIT,
            MORPHOLOGY_ANALYSIS_REVIEW,
            MORPHOLOGY_ANALYSIS_PUBLISH,
            MORPHOLOGY_RULE_VIEW,
            MORPHOLOGY_RULE_MANAGE,
            GRAMMAR_TOPIC_VIEW,
            GRAMMAR_TOPIC_MANAGE,
            GRAMMAR_RULE_VIEW,
            GRAMMAR_RULE_CREATE,
            GRAMMAR_RULE_EDIT,
            GRAMMAR_RULE_SUBMIT,
            GRAMMAR_RULE_REVIEW,
            GRAMMAR_RULE_PUBLISH,
            GRAMMAR_RULE_ARCHIVE,
            GRAMMAR_CONCEPT_VIEW,
            GRAMMAR_CONCEPT_MANAGE,
            GRAMMAR_EXAMPLE_MANAGE,
            GRAMMAR_ANNOTATION_MANAGE,
            SEARCH_ADMIN_VIEW,
            SEARCH_REINDEX);

    private PermissionCatalog() {
    }

    public static List<String> all() {
        return ALL;
    }

    public static boolean exists(String code) {
        return ALL.contains(code);
    }

    public static Set<String> copyOf(Iterable<String> codes) {
        return Set.copyOf(ALL.stream().filter(code -> {
            for (String candidate : codes) {
                if (code.equals(candidate)) {
                    return true;
                }
            }
            return false;
        }).toList());
    }
}
