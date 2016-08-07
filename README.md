# Sandbox-Jenkins-Plugin

[![Stories in Ready](https://badge.waffle.io/QualiSystems/Sandbox-Jenkins-Plugin.svg?label=ready&title=Ready)](http://waffle.io/QualiSystems/Sandbox-Jenkins-Plugin)
[![Dependency Status](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin/badge)](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin)

## Installation
1) Download the hpi package from the releases tab

2) Navigate to the advanced section under the plugins tab in jenkins

3) Upload the hpi file into the "upload plugin" section

4) Restart jenkins

## Configuring CloudShell in Jenkins
1) Navigate to the main Jenkins settings page

2) Fill all fields under "cloudshell configuraiton" section.

![Alt text](Pics/mainsetting.png?raw=true)

### Pipeline support (Workflow) - New!
![Alt text](Pics/pipeline.png?raw=true)

### Adding build steps
Use a pre-scm step to start a sandbox and a post-build step for stopping running sandboxes.

node: make sure to check the "Fail the build on error" when using the pre-scm step, this will fail the build in case the sandbox will fail to create.

Pre-scm step:

![Alt text](Pics/PreSCM.png?raw=true)

Post build step:

![Alt text](Pics/postBuild.png?raw=true)

Enjoy
