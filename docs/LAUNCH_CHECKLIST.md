# Launch checklist

Complete this when a host and a real domain exist. S13 does not check these boxes.

## Infrastructure

- [ ] Domain chosen
- [ ] DNS A or CNAME in place
- [ ] HTTPS certificate renewing automatically
- [ ] HTTP redirected to HTTPS
- [ ] PostgreSQL reachable only from the backend
- [ ] Redis reachable only from the backend
- [ ] Backup scheduled, encrypted, and one restore rehearsed on that host
- [ ] Secrets in the host store, not in Git
- [ ] `SITE_URL` and `FRONTEND_URL` are the real https origin
- [ ] `NEXT_PUBLIC_API_URL` matches the API origin
- [ ] CORS origin is that public origin only
- [ ] `TRUSTED_PROXIES` set to the proxy that connects to the backend
- [ ] `ENABLE_HSTS=true` only after HTTPS works
- [ ] Liveness and readiness checked

## Product

- [ ] Homepage
- [ ] Search
- [ ] Dictionary word and root
- [ ] Grammar
- [ ] Morphology
- [ ] Spelling, rhetoric, literature, articles
- [ ] Tools and one tool result
- [ ] Assistant shows a safe state with AI off, or a grounded answer with AI on
- [ ] Learning path, lesson, and quiz
- [ ] Admin login and the editorial room

## SEO

- [ ] `SITE_URL` is the live origin
- [ ] `SEO_INDEXING_ENABLED=true` only after the domain serves the right pages
- [ ] `robots.txt` allows public pages and disallows `/admin/`, `/api/`, and `/search`
- [ ] `sitemap.xml` lists published URLs only
- [ ] Canonicals use the live origin
- [ ] Structured data matches the visible page
- [ ] Unknown URLs are 404

## Security

- [ ] No default or `local-dev-only` password in production
- [ ] JWT secret generated for production and not reused from development
- [ ] Owner password changed after bootstrap
- [ ] Login rate limit returns 429
- [ ] Security headers present on a public page and on an API response
- [ ] A backup has been restored once
