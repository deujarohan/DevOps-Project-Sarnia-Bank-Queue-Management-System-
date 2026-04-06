Your Laptop Azure VM (in the cloud)
| |
You push code to GitHub
→ GitHub Actions builds & tests it
→ Copies the JAR to Azure VM
→ Starts the app on Azure VM
|
App runs 24/7 on the VM
|
Anyone in the world can access it at
http://YOUR-VM-IP:8080/index.html

git push -u origin (branch name)
