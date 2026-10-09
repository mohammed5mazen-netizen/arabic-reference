# المرجع العربي — Arabic Reference

منصة معرفة لغوية عربية طويلة الأجل. المرحلة الحالية هي **S13: Production Hardening & Final Launch Readiness**. غرفة العمليات في `/admin/editorial` للفريق الداخلي فقط. التعلّم في `/learn` يقرأ المسارات والدروس المنشورة من دون حساب، والاختبار يُصحَّح في الخادم. المساعد اللغوي في `/assistant` يجيب من المعرفة المنشورة فقط. يمكن إيقافه من البيئة دون أن يتوقف بقية الموقع. مركز الأدوات في `/tools` يقرأ المعرفة المنشورة. مربع البحث في `/` و`/search` يبحث في المعجم والجذور والنحو والمحتوى والدروس المنشورة. الإملاء في `/spelling`، والبلاغة في `/rhetoric`، والأدب في `/literature`، والمقالات في `/articles`. النحو في `/grammar` يبقى مرجعًا منظّمًا.

المرجع مفتوح للقراءة. الزائر يصل من النطاق إلى الصفحة الرئيسية ثم إلى البحث والتصفح، دون حساب ودون شاشة دخول. دخول الإدارة في `/admin/login` لفريق التحرير فقط.

> Arabic Reference is an open linguistic reference. Authentication must never become a prerequisite for ordinary access to public linguistic knowledge.

## الرؤية

ستجمع المنصة لاحقًا المعجم، الجذور، الصرف، النحو، الإملاء، البلاغة، الأدب، المصادر، الأدوات، والتعليم، ومساعدًا لغويًا. المعلومة الموثّقة بمصدرها هي الأصل. الذكاء الاصطناعي مساعد، وليس سلطة لغوية.

## المعمارية

Modular monolith وAPI-first. الخلفية Java 25 وSpring Boot 4.1.1. الواجهة Next.js وTypeScript واتجاه RTL عربي. PostgreSQL هو سجل النظام، وRedis للحدود المؤقتة والتخزين القصير. الإنتاج أربع عمليات: الواجهة، الخلفية، PostgreSQL، وRedis. التفاصيل في [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) و[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).

```
arabic-reference/
├── backend/
├── frontend/
├── docs/
├── infrastructure/
├── scripts/
├── .github/
├── docker-compose.yml
├── docker-compose.prod.yml
├── .env.example
└── README.md
```

## المتطلبات

- JDK 25
- Node.js 24 وnpm
- Docker مع Docker Compose
- لا حاجة إلى تثبيت Maven على الجهاز؛ المشروع يستخدم Maven Wrapper

## التشغيل المحلي

من PowerShell في جذر المستودع:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

أو:

```powershell
.\scripts\dev.ps1
```

القيم `local-dev-only` في `.env.example` خاصة بالتشغيل المحلي داخل Docker. لا تستخدمها في أي بيئة مشتركة، ولا تضع كلمة مرور حقيقية في Git.

الخلفية تقرأ المتغيرات من البيئة، وقيم `application.yml` الافتراضية تطابق Docker Compose المحلي، لذلك يمكن التشغيل بعد `docker compose up` دون تحميل ملف `.env` يدويًا.

### الخلفية

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

الصحة: `http://localhost:8080/actuator/health`

الأساس العام: `http://localhost:8080/api/v1/public/foundation`

### الواجهة

```powershell
cd frontend
npm install
npm run dev
```

الصفحة الرئيسية: `http://localhost:3000`

دخول الإدارة، بعد ضبط متغيرات المالك: `http://localhost:3000/admin/login`

## الاختبارات

```powershell
cd backend
.\mvnw.cmd clean verify
```

```powershell
cd frontend
npm install
npm run lint
npm run typecheck
npm run build
npm test
```

اختبارات التكامل تستخدم Testcontainers، وتحتاج Docker قيد التشغيل. لا يُستخدم H2 بدل PostgreSQL. GitHub Actions يشغّل التحقق نفسه ولا يتصل بقاعدة إنتاج.

## الإنتاج

`docker compose -f docker-compose.prod.yml` لا ينشر منافذ PostgreSQL أو Redis. الواجهة تُبنى بـ `next build` وتُشغَّل بـ `node server.js`، لا بـ `next dev`. ملف التعريف `prod` يرفض أسرار التطوير المحلي. راجع [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) و[docs/LAUNCH_CHECKLIST.md](docs/LAUNCH_CHECKLIST.md) و[docs/PRODUCTION_RUNBOOK.md](docs/PRODUCTION_RUNBOOK.md).

قرار الترخيص لم يُحسم. لا تُضف رخصة مفتوحة إلى أن يقرر المالك. المستودع لا يُحوَّل إلى عام من هذه المرحلة.

## متغيرات البيئة

| المتغير | الغرض |
| --- | --- |
| `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | PostgreSQL |
| `REDIS_HOST` `REDIS_PORT` `REDIS_PASSWORD` | Redis |
| `SERVER_PORT` | منفذ الخلفية |
| `FRONTEND_URL` | أصل CORS للواجهة |
| `SITE_URL` | أصل العنوان المعياري. في الإنتاج يجب أن يكون https عامًا. `NEXT_PUBLIC_SITE_URL` يُستخدم فقط إذا كان `SITE_URL` فارغًا |
| `SEO_INDEXING_ENABLED` | الفهرسة. تبقى `false` حتى يعمل الدومين النهائي |
| `NEXT_PUBLIC_API_URL` | أصل واجهة البرمجة الذي تستدعيه الواجهة |
| `ADMIN_JWT_SECRET` | سر توقيع رمز الدخول الإداري، 32 بايتًا على الأقل |
| `TRUSTED_PROXIES` | عناوين الوكيل الموثوق. فارغ يعني تجاهل `X-Forwarded-For` |
| `ENABLE_HSTS` | `true` فقط بعد HTTPS. التطوير المحلي يبقى `false` |
| `DB_POOL_SIZE` | حد تجمع الاتصالات. الافتراضي 20 |
| `BOOTSTRAP_OWNER_USERNAME` `BOOTSTRAP_OWNER_EMAIL` `BOOTSTRAP_OWNER_DISPLAY_NAME` `BOOTSTRAP_OWNER_PASSWORD` | إنشاء مالك المنصة مرة واحدة إذا لم يوجد مالك. في ملف `prod` يُطلب تغيير كلمة المرور |
| `AI_ENABLED` `AI_PROVIDER` `AI_MODEL` `AI_API_KEY` | المساعد اللغوي. المفتاح في البيئة فقط، والقيمة الافتراضية إيقاف المساعد |

## ما الذي لا يوجد بعد

لا تسجيل عام، ولا دخول للزائر، ولا محلل جمل، ولا تشكيل آلي كامل، ولا مصحح إملائي آلي، ولا وكيل يبحث في الإنترنت أو ينشر في المرجع. المساعد اللغوي يقرأ المعرفة المنشورة ولا يستبدل المراجعة. المصادقة الحالية للإدارة والتحرير فقط. حدود الصرف موثّقة في [docs/MORPHOLOGY_ENGINE.md](docs/MORPHOLOGY_ENGINE.md)، وسياسة التوثيق في [docs/AI_GROUNDING_POLICY.md](docs/AI_GROUNDING_POLICY.md).
