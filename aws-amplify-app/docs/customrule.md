# AWS::Amplify::App CustomRule

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#condition" title="Condition">Condition</a>" : <i>String</i>,
    "<a href="#status" title="Status">Status</a>" : <i>String</i>,
    "<a href="#target" title="Target">Target</a>" : <i>String</i>,
    "<a href="#source" title="Source">Source</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#condition" title="Condition">Condition</a>: <i>String</i>
<a href="#status" title="Status">Status</a>: <i>String</i>
<a href="#target" title="Target">Target</a>: <i>String</i>
<a href="#source" title="Source">Source</a>: <i>String</i>
</pre>

## Properties

#### Condition

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>(?s).*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Status

_Required_: No

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>7</code>

_Pattern_: <code>.{3,7}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Target

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>(?s).+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Source

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>(?s).+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
