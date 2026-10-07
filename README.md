# LMS Backend 
This is a RESTful API for a learning management system (LMS). The API allows you to perform CRUD operations on authenticated users, courses, and enrollments.

## Prerequisites
- Java 21
- Maven
- Supabase

## Getting Started

1. Clone the repository
2. Run `mvn clean install`
3. Run `java -jar target/course-0.0.1-SNAPSHOT.jar`
4. The API will be available at `http://localhost:8080/api`

## API Documentation
The API documentation is available at `APIs.md` file.

## Production CORS and availability

Set `CORS_ALLOWED_ORIGINS` on the backend service to the comma-separated frontend origins (no trailing slash), for example `https://lms-ujuzi.vercel.app,http://localhost:5173`. The configured value replaces the default in `application.properties`; deploy/restart the backend after changing it. Only add other Vercel preview domains if needed, rather than allowing every `*.vercel.app` site with credentials.

To check the deployed backend from a terminal:

```sh
curl -i -X OPTIONS 'https://backend-m9ax.onrender.com/api/courses' \
  -H 'Origin: https://lms-ujuzi.vercel.app' \
  -H 'Access-Control-Request-Method: GET' \
  -H 'Access-Control-Request-Headers: authorization,content-type'
```

Expect HTTP 200 and `Access-Control-Allow-Origin: https://lms-ujuzi.vercel.app`. HTTP 403 with `Invalid CORS request` means the running deployment still does not allow that origin. If even `GET /` is slow or times out, check Render service logs and startup/database connectivity; a sleeping or starting instance can outlast a 10-second frontend timeout. `/healthz` also queries the database, so it can be slow while the DB is unavailable.

## Database
The database is configured in `application.properties`. The default configuration is for a Supabase-hosted database.

## Tests
The API can be tested using the `TESTs.md` file.

