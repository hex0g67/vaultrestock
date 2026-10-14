# VaultRestocker (Paper 26.3)

Clears every vault's "already rewarded" list every 30 seconds so players can reopen it with a new key.

## Build
Requires JDK 25 and Maven:

    mvn clean package

Jar: target/VaultRestocker-1.0.0.jar -> copy into your server's plugins/ folder.

No local setup? Push this folder to a GitHub repo, open the Actions tab,
run "Build plugin", and download the VaultRestocker-jar artifact.

## Commands
/vaultrestock now     restock immediately
/vaultrestock reload  reload config.yml
