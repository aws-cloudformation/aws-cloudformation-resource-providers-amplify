# AWS::Amplify::Domain SubDomainSetting

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#prefix" title="Prefix">Prefix</a>" : <i>String</i>,
    "<a href="#branchname" title="BranchName">BranchName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#prefix" title="Prefix">Prefix</a>: <i>String</i>
<a href="#branchname" title="BranchName">BranchName</a>: <i>String</i>
</pre>

## Properties

#### Prefix

_Required_: Yes

_Type_: String

_Maximum_: <code>255</code>

_Pattern_: <code>(?s).*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BranchName

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
