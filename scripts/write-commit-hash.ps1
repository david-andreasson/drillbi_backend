# Skriv senaste git commit-hash till BACKEND_VERSION.txt
$hash = git rev-parse HEAD
Set-Content -Path "../BACKEND_VERSION.txt" -Value $hash
