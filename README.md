# Library Management System — GitHub + Vercel Ready

## Architecture
- frontend/: HTML/CSS/JavaScript — deploy this folder to Vercel
- backend/: Java 17 + Spring Boot REST API — deploy to Railway/Render/etc.
- MySQL: production database

## Local run
1. Create MySQL database: `CREATE DATABASE library_db;`
2. In backend/src/main/resources/application.properties set your local MySQL password.
3. Run backend: `cd backend && mvn spring-boot:run`
4. Open frontend/index.html locally. The default API is http://localhost:8080/api.

## GitHub
Create a repository and upload the whole project. Keep frontend and backend in the same repository.

## Vercel
In Vercel, import the GitHub repository. Set Root Directory to `frontend`, Framework Preset to `Other`, then deploy.
After the Java backend is deployed, set the Vercel environment variable:
`LIBRARY_API_URL=https://YOUR-BACKEND-URL/api`
Redeploy the frontend.

## Important
Do NOT put MySQL passwords in frontend files or GitHub.
The Java backend must be hosted separately because Vercel does not run a persistent Spring Boot server.

## BTech features
Book catalog/search, student management, issue/return, availability tracking, due dates, automatic ₹5/day late fine, dashboard statistics.
