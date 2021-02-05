# AWS::Amplify::Domain

The AWS::Amplify::Domain resource allows you to connect a custom domain to your app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Amplify::Domain",
    "Properties" : {
        "<a href="#appid" title="AppId">AppId</a>" : <i>String</i>,
        "<a href="#autosubdomaincreationpatterns" title="AutoSubDomainCreationPatterns">AutoSubDomainCreationPatterns</a>" : <i>[ String, ... ]</i>,
        "<a href="#autosubdomainiamrole" title="AutoSubDomainIAMRole">AutoSubDomainIAMRole</a>" : <i>String</i>,
        "<a href="#domainname" title="DomainName">DomainName</a>" : <i>String</i>,
        "<a href="#enableautosubdomain" title="EnableAutoSubDomain">EnableAutoSubDomain</a>" : <i>Boolean</i>,
        "<a href="#subdomainsettings" title="SubDomainSettings">SubDomainSettings</a>" : <i>[ <a href="subdomainsetting.md">SubDomainSetting</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Amplify::Domain
Properties:
    <a href="#appid" title="AppId">AppId</a>: <i>String</i>
    <a href="#autosubdomaincreationpatterns" title="AutoSubDomainCreationPatterns">AutoSubDomainCreationPatterns</a>: <i>
      - String</i>
    <a href="#autosubdomainiamrole" title="AutoSubDomainIAMRole">AutoSubDomainIAMRole</a>: <i>String</i>
    <a href="#domainname" title="DomainName">DomainName</a>: <i>String</i>
    <a href="#enableautosubdomain" title="EnableAutoSubDomain">EnableAutoSubDomain</a>: <i>Boolean</i>
    <a href="#subdomainsettings" title="SubDomainSettings">SubDomainSettings</a>: <i>
      - <a href="subdomainsetting.md">SubDomainSetting</a></i>
</pre>

## Properties

#### AppId

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AutoSubDomainCreationPatterns

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoSubDomainIAMRole

_Required_: No

_Type_: String

_Maximum_: <code>1000</code>

_Pattern_: <code>^$|^arn:aws:iam::\d{12}:role.+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DomainName

_Required_: Yes

_Type_: String

_Maximum_: <code>255</code>

_Pattern_: <code>^(((?!-)[A-Za-z0-9-]{0,62}[A-Za-z0-9])\.)+((?!-)[A-Za-z0-9-]{1,62}[A-Za-z0-9])(\.)?$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EnableAutoSubDomain

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SubDomainSettings

_Required_: Yes

_Type_: List of <a href="subdomainsetting.md">SubDomainSetting</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

#### DomainStatus

Returns the <code>DomainStatus</code> value.

#### StatusReason

Returns the <code>StatusReason</code> value.

#### CertificateRecord

Returns the <code>CertificateRecord</code> value.
