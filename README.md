# CloudShell Sandbox Jenkins Plugin

[![Stories in Ready](https://badge.waffle.io/QualiSystems/Sandbox-Jenkins-Plugin.svg?label=ready&title=Ready)](http://waffle.io/QualiSystems/Sandbox-Jenkins-Plugin)
[![Dependency Status](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin/badge)](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin)

## Introduction

The **CloudShell Sandbox Jenkins Plugin** provides an easy way to consume CloudShell sandboxes through Jenkins for a variety of use cases. The plugin allows you to build jobs that create on demand sandboxes in CloudShell based on pre-defined blueprints.


## Requirements
This plugin requires CloudShell 8.0 or later and Jenkins 2.0 or later. 
Note: Depending on the plugin version, some features may require a specific version of CloudShell. For more details, see the features section.


## Architecture
The **CloudShell Sandbox Jenkins Plugin** leverages CloudShell sandbox API to perform operations in CloudShell. CloudShell Sandbox API comes out of the box with the CloudShell Suite installation and should be fully installed and configured for the plugin functionality.
When configuring the CloudShell Sandbox API, you will need to set the API port (82 by default). To see the port, open the **CloudShell Configuration** application and click **CloudShell Sandbox API**. 

![Alt text](Pics/ConfigurationWizard.png?raw=true)

The **Quali Configuration** web interface is displayed, showing the Sandbox API configurations.

![Alt text](Pics/APIConfig.png?raw=true)

Distributed architecture:
![Alt text](Pics/Jenkinspluginarchitecture.jpg?raw=true)

## Configuration
After installing the plugin, perform the following steps:

1.	Navigate to the main Jenkins configuration page (**Manage Jenkins > Configure System**) and configure the plugin according to your CloudShell installation.
2.	Set the **CloudShell Sandbox API** Host Address to the machine where CloudShell Sandbox API is installed.
    Note that this may be a different machine than the Quali Server.
3.	Specify the credentials (user, password, domain) of the CloudShell user you would like to use for CloudShell operations.
We recommend creating a new CloudShell admin user for Jenkins.
4.	To verify your configurations, click the **Test Connection**. Jenkins will to interact with CloudShell to validate connectivity and credentials.

![Alt text](Pics/ConfigurationPage.png?raw=true)

## Freestyle Steps
The plugin adds several new steps to Jenkins to streamline interactions with CloudShell sandboxes.
**CloudShell Build Step** is a generic step that contains CloudShell Actions you can execute. Each action contains several inputs. Currently, the action **Start sandbox** is provided and we plan to support others in the future.

The **Start Sandbox** action creates a new CloudShell sandbox based on the selected blueprint and restricts interaction with the sandbox while it is running Setup. This ensures the sandbox Setup process completes successfully without any outside interference. When the sandbox is active, the sandbox’s Id and additional information become available in $SANDBOX_ID and $SANDBOX_DETAILS, respectively. These environment variables can be used in other steps in the build.
Note that the **Sandbox duration in minutes** field specifies the maximum duration of the sandbox. If the build does not end before the specified duration, CloudShell will tear down the sandbox.
For more information about a field, click that field’s help icon on the right.

![Alt text](Pics/StartSandboxAction.png?raw=true)

**We recommend using the “Start Sandbox” action as a pre-run step to ensure the sandbox is created before the actual build steps are executed.**

Here is an example of how to print the sandbox information for future use:
![Alt text](Pics/echoSandboxInformation.png?raw=true)

To end the sandboxes that have been created in the build, use the **Stop CloudShell sandboxes** post-build action. Since this is a post-build action, it can be used only once per build and will end all sandboxes created by that build. This step ensures that the sandbox Teardown process completes successfully and checks the sandbox’s Activity Feed to validate that there are no errors in the sandbox activity log.
![Alt text](Pics/StopAction.png?raw=true)

## Pipeline Steps and Syntax
The plugin installation adds the following pipeline steps to the Jenkins pipeline: 
  * **startSandbox** – Initiates a new sandbox in CloudShell and waits for the sandbox to complete its Setup process. The method returns the sandbox Id.
  * **stopSandbox** – Stops an active sandbox, waits for the teardown process to end and checks the sandbox’s Activity Feed for errors.
  
Here is an example of how to use the pipeline syntax to execute a Python test that requires the use of a sandbox that is based on a “Performance” blueprint: 
1.	First, the Performance blueprint is reserved using the parameters passed from Jenkins.
2.	The sandbox runs the Setup process.
3.	When this process completes successfully, Jenkins runs the Python code (downloaded from the build VCS) with the sandbox Id. 
4.	At the end of the test, the plugin initiates the sandbox’s Teardown process using the **stopSandbox** method.
 
 ```
    stage ('Performance Testing'){
        Id = startSandbox duration: 13, name: 'Performance', params: 'os=Win; server=4'
        sh 'python .\Perf2.py --sandbox_id Id'
        stopSandbox  Id
    }
 ```

Note: The **WithSandbox** step implements the same logic as in **startSandbox** and **stopSandbox** but in a contextual syntax. This step is recommended for demos but not suitable for production.

We recommend using the Jenkins pipeline’s **Snippet Generator** which allows you to compose pipelines with an easy-to-use UI instead of having to write code. For example:
![Alt text](Pics/PipelineSnippet.png?raw=true)

## Features
This table lists the plugin features that are supported per CloudShell version:

Plugin feature | CloudShell version
--- | ---
Blueprint parameters | 8.0 and up
Activity Feed Teardown validation | 8.1 and up


## Contributing and issues
The plugin is an open source project under the MIT License. We encourage users to contribute, add pull requests and open issues.