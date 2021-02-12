# AWS::Amplify::Branch

The AWS::Amplify::Branch resource creates a new branch within an app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Amplify::Branch",
    "Properties" : {
        "<a href="#appid" title="AppId">AppId</a>" : <i>String</i>,
        "<a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>" : <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>,
        "<a href="#branchname" title="BranchName">BranchName</a>" : <i>String</i>,
        "<a href="#buildspec" title="BuildSpec">BuildSpec</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#enableautobuild" title="EnableAutoBuild">EnableAutoBuild</a>" : <i>Boolean</i>,
        "<a href="#enableperformancemode" title="EnablePerformanceMode">EnablePerformanceMode</a>" : <i>Boolean</i>,
        "<a href="#enablepullrequestpreview" title="EnablePullRequestPreview">EnablePullRequestPreview</a>" : <i>Boolean</i>,
        "<a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>" : <i>[ <a href="environmentvariable.md">EnvironmentVariable</a>, ... ]</i>,
        "<a href="#pullrequestenvironmentname" title="PullRequestEnvironmentName">PullRequestEnvironmentName</a>" : <i>String</i>,
        "<a href="#stage" title="Stage">Stage</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Amplify::Branch
Properties:
    <a href="#appid" title="AppId">AppId</a>: <i>String</i>
    <a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>: <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>
    <a href="#branchname" title="BranchName">BranchName</a>: <i>String</i>
    <a href="#buildspec" title="BuildSpec">BuildSpec</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#enableautobuild" title="EnableAutoBuild">EnableAutoBuild</a>: <i>Boolean</i>
    <a href="#enableperformancemode" title="EnablePerformanceMode">EnablePerformanceMode</a>: <i>Boolean</i>
    <a href="#enablepullrequestpreview" title="EnablePullRequestPreview">EnablePullRequestPreview</a>: <i>Boolean</i>
    <a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>: <i>
      - <a href="environmentvariable.md">EnvironmentVariable</a></i>
    <a href="#pullrequestenvironmentname" title="PullRequestEnvironmentName">PullRequestEnvironmentName</a>: <i>String</i>
    <a href="#stage" title="Stage">Stage</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### AppId

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### BasicAuthConfig

_Required_: No

_Type_: <a href="basicauthconfig.md">BasicAuthConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BranchName

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### BuildSpec

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

_Required_: No

_Type_: String

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

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.
