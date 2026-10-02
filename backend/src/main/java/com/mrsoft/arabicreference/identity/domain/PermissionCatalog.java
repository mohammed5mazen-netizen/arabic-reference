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
            CITATION_MANAGE);

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
