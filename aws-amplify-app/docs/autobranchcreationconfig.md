# AWS::Amplify::App AutoBranchCreationConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#autobranchcreationpatterns" title="AutoBranchCreationPatterns">AutoBranchCreationPatterns</a>" : <i>[ String, ... ]</i>,
    "<a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>" : <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>,
    "<a href="#buildspec" title="BuildSpec">BuildSpec</a>" : <i>String</i>,
    "<a href="#enableautobranchcreation" title="EnableAutoBranchCreation">EnableAutoBranchCreation</a>" : <i>Boolean</i>,
    "<a href="#enableautobuild" title="EnableAutoBuild">EnableAutoBuild</a>" : <i>Boolean</i>,
    "<a href="#enableperformancemode" title="EnablePerformanceMode">EnablePerformanceMode</a>" : <i>Boolean</i>,
    "<a href="#enablepullrequestpreview" title="EnablePullRequestPreview">EnablePullRequestPreview</a>" : <i>Boolean</i>,
    "<a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>" : <i>[ <a href="environmentvariable.md">EnvironmentVariable</a>, ... ]</i>,
    "<a href="#pullrequestenvironmentname" title="PullRequestEnvironmentName">PullRequestEnvironmentName</a>" : <i>String</i>,
    "<a href="#stage" title="Stage">Stage</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#autobranchcreationpatterns" title="AutoBranchCreationPatterns">AutoBranchCreationPatterns</a>: <i>
      - String</i>
<a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>: <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>
<a href="#buildspec" title="BuildSpec">BuildSpec</a>: <i>String</i>
<a href="#enableautobranchcreation" title="EnableAutoBranchCreation">EnableAutoBranchCreation</a>: <i>Boolean</i>
<a href="#enableautobuild" title="EnableAutoBuild">EnableAutoBuild</a>: <i>Boolean</i>
<a href="#enableperformancemode" title="EnablePerformanceMode">EnablePerformanceMode</a>: <i>Boolean</i>
<a href="#enablepullrequestpreview" title="EnablePullRequestPreview">EnablePullRequestPreview</a>: <i>Boolean</i>
<a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>: <i>
      - <a href="environmentvariable.md">EnvironmentVariable</a></i>
<a href="#pullrequestenvironmentname" title="PullRequestEnvironmentName">PullRequestEnvironmentName</a>: <i>String</i>
<a href="#stage" title="Stage">Stage</a>: <i>String</i>
</pre>

## Properties

#### AutoBranchCreationPatterns

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BasicAuthConfig

_Required_: No

_Type_: <a href="basicauthconfig.md">BasicAuthConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BuildSpec

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>25000</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnableAutoBranchCreation

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnableAutoBuild

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnablePerformanceMode

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnablePullRequestPreview

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnvironmentVariables

_Required_: No

_Type_: List of <a href="environmentvariable.md">EnvironmentVariable</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PullRequestEnvironmentName

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Stage

_Required_: No

_Type_: String

_Allowed Values_: <code>EXPERIMENTAL</code> | <code>BETA</code> | <code>PULL_REQUEST</code> | <code>PRODUCTION</code> | <code>DEVELOPMENT</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
