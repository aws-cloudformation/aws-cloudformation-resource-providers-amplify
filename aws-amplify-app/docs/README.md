# AWS::Amplify::App

Resource Type definition for AWS::Amplify::App

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Amplify::App",
    "Properties" : {
        "<a href="#accesstoken" title="AccessToken">AccessToken</a>" : <i>String</i>,
        "<a href="#autobranchcreationconfig" title="AutoBranchCreationConfig">AutoBranchCreationConfig</a>" : <i><a href="autobranchcreationconfig.md">AutoBranchCreationConfig</a></i>,
        "<a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>" : <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>,
        "<a href="#buildspec" title="BuildSpec">BuildSpec</a>" : <i>String</i>,
        "<a href="#customheaders" title="CustomHeaders">CustomHeaders</a>" : <i>String</i>,
        "<a href="#customrules" title="CustomRules">CustomRules</a>" : <i>[ <a href="customrule.md">CustomRule</a>, ... ]</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#enablebranchautodeletion" title="EnableBranchAutoDeletion">EnableBranchAutoDeletion</a>" : <i>Boolean</i>,
        "<a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>" : <i>[ <a href="environmentvariable.md">EnvironmentVariable</a>, ... ]</i>,
        "<a href="#iamservicerole" title="IAMServiceRole">IAMServiceRole</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#repository" title="Repository">Repository</a>" : <i>String</i>,
        "<a href="#oauthtoken" title="OauthToken">OauthToken</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Amplify::App
Properties:
    <a href="#accesstoken" title="AccessToken">AccessToken</a>: <i>String</i>
    <a href="#autobranchcreationconfig" title="AutoBranchCreationConfig">AutoBranchCreationConfig</a>: <i><a href="autobranchcreationconfig.md">AutoBranchCreationConfig</a></i>
    <a href="#basicauthconfig" title="BasicAuthConfig">BasicAuthConfig</a>: <i><a href="basicauthconfig.md">BasicAuthConfig</a></i>
    <a href="#buildspec" title="BuildSpec">BuildSpec</a>: <i>String</i>
    <a href="#customheaders" title="CustomHeaders">CustomHeaders</a>: <i>String</i>
    <a href="#customrules" title="CustomRules">CustomRules</a>: <i>
      - <a href="customrule.md">CustomRule</a></i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#enablebranchautodeletion" title="EnableBranchAutoDeletion">EnableBranchAutoDeletion</a>: <i>Boolean</i>
    <a href="#environmentvariables" title="EnvironmentVariables">EnvironmentVariables</a>: <i>
      - <a href="environmentvariable.md">EnvironmentVariable</a></i>
    <a href="#iamservicerole" title="IAMServiceRole">IAMServiceRole</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#repository" title="Repository">Repository</a>: <i>String</i>
    <a href="#oauthtoken" title="OauthToken">OauthToken</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### AccessToken

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoBranchCreationConfig

_Required_: No

_Type_: <a href="autobranchcreationconfig.md">AutoBranchCreationConfig</a>

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

#### CustomHeaders

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CustomRules

_Required_: No

_Type_: List of <a href="customrule.md">CustomRule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

_Required_: No

_Type_: String

_Maximum_: <code>1000</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnableBranchAutoDeletion

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnvironmentVariables

_Required_: No

_Type_: List of <a href="environmentvariable.md">EnvironmentVariable</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IAMServiceRole

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

_Required_: Yes

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Repository

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OauthToken

_Required_: No

_Type_: String

_Maximum_: <code>1000</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the AppId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AppId

Returns the <code>AppId</code> value.

#### AppName

Returns the <code>AppName</code> value.

#### Arn

Returns the <code>Arn</code> value.

#### DefaultDomain

Returns the <code>DefaultDomain</code> value.
