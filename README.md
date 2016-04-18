# Sandbox-Jenkins-Plugin

## Installation
1) download the hpi package from the releases tab
2) navigate to the advanced section under the plugins tab in jenkins
3) upload the hpi file into the "upload plugin" section
4) restart jenkins

## main configuration
1) navigate to the main Jenkins settings page
2) fill all fields under "cloudshell configuraiton" section.

![Alt text](Pics/mainsetting.png?raw=true)

### build configuration
Use a pre-scm step to start a sandbox and a post-build step for stopping running sandboxes.
node: make sure to check the "Fail the build on error" when using the pre-scm step, this will fail the build in case the sandbox will fail to create.

Pre-scm step:
![Alt text](Pics/PreSCM.png?raw=true)

Post build step:
![Alt text](Pics/postBuild.png?raw=true)

Enjoy
