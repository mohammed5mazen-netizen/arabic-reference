INSERT INTO admin_permission (code, description) VALUES
    ('seo.admin.view', 'View SEO indexing status');

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'seo.admin.view'),
    ('a0000000-0000-4000-8000-000000000002', 'seo.admin.view'),
    ('a0000000-0000-4000-8000-000000000006', 'seo.admin.view');
