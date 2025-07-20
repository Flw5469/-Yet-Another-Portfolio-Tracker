@echo off
echo Starting PostgreSQL container...
docker start crypto-postgres

if %errorlevel% neq 0 (
    echo Failed to start container
    pause
    exit /b 1
)

echo Connecting to database...
docker exec -it crypto-postgres psql -U postgres -d crypto_db
pause