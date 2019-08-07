<!DOCTYPE html>
<html lang="en">
<head>

    <title>Parabuild CI | ${title?html}</title>

    <#-- Meta -->
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="robots" content="index, follow, noarchive">
    <meta name='gwt:property' content='locale=en_US'>

    <#-- Link -->
    <link rel='stylesheet' type='text/css' href='https://fonts.googleapis.com/css?family=Lato:400,700'/>
    <link rel="stylesheet" type="text/css" href="${base}/parabuild-ci.css">

    <#-- This script tag is what actually loads the GWT module.  The 'nocache.js' file
    (also called a "selection script") is produced by the GWT compiler in the module output
    directory or generated automatically in development mode. -->
    <script src="${base}/repository/repository.nocache.js" type="text/javascript"></script>

    ${head}
</head>

<body>
<div id="wrap">
    <div id="header">
        <div class="logo">
            <@s.a action="/index" title="Go to Parabuild CI home">
                <img src="/images/theme/parabuildci_logo.jpg" alt="Build and Deploy with Pleasure!" height="70"/>
                <img src="/images/theme/parabuildci_byline.jpg" alt="Build and Deploy with Pleasure!" height="70"/>
            </@s.a>
        </div>

        <#-- Top user menu -->
        <div id="user">
            <#if Session.loggedInUser??>
                |
                <@s.a action="editUserProfile" title="%{getText('edit.profile.for.user')} '${Session.loggedInUser.name?html}'">${Session.loggedInUser.name?html}</@s.a>
            <#else>
                | <@s.a href="%{welcome}" title="%{getText('Register.with.Joglet')}"><@s.text name="Join.for.FREE"/></@s.a>
            </#if>
        </div>

        <ul id="nav">
            <#-- Home -->
            <li class="tab home <#if contextMenu?? && contextMenu = "home">selected</#if>">
                <@s.a action="index" title="Home">Home</@s.a>
            </li>

            <#-- Projects -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "projects">selected</#if>">
                <@s.a action="projects" title="Projects">Projects</@s.a>
            </li>

            <#-- Builds -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "builds">selected</#if>">
                <@s.a action="builds" title="Builds">Builds</@s.a>
            </li>

            <#-- Results -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "results">selected</#if>">
                <@s.a action="results" title="Results">Results</@s.a>
            </li>

            <#-- Administration -->
            <#if Session.loggedInUser?? && Session.loggedInUser.admin>
                <li class="tab normal <#if contextMenu?? && contextMenu = "administration">selected</#if>">
                    <@s.a action="administration" title="Manage Parabuild CI">Administration</@s.a>
                    <ul>
                        <li><@s.a action="showSystemParameters" title="%{getText('manage.system.parameters')}">
                                <@s.text name="System.Parameters"/></@s.a></li>
                        <li><@s.a action="listMediaServers" title="%{getText('manage.wowza.servers')}">
                                <@s.text name="Wowza.Servers"/></@s.a></li>
                        <li>
                            <@s.url id="userList" action="userList">
                                <@s.param name="search"></@s.param>
                            </@s.url>
                            <@s.a href="%{userList}" title="%{getText('Manage.Joglet.users')}"><@s.text name="Users"/></@s.a>
                        </li>
                        <li><@s.a action="reports" title="%{getText('View.Reports')}"><@s.text name="Reports"/></@s.a></li>
                        <li><@s.a action="sendEmailToAllUsers" title="%{getText('Send.Email.to.All.Users')}"><@s.text name="Send.Email.to.All.Users"/></@s.a></li>
                        <li><@s.a action="sendWelcomeMessage" title="%{getText('send.welcome.messages')}"><@s.text name="Send.Welcome.Messages"/></@s.a></li>
                        <li><@s.a action="sendFriendJoinedMessage" title="%{getText('Send.friend.joined.messages')}"><@s.text name="Send.Friend.Joined.Messages"/></@s.a></li>
                    </ul>
                </li>
            </#if>


            <#-- Errors -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "errors">selected</#if>">
                <@s.a action="results" title="Errors">Errors</@s.a>
            </li>

            <#-- Search -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "search">selected</#if>">
                <@s.a action="search" title="Search">Search</@s.a>
            </li>

            <#-- Help -->
            <li class="tab normal <#if contextMenu?? && contextMenu = "help">selected</#if>">
                <@s.a action="help" title="Get help with Parabuild">Help</@s.a>
                <ul>
                    <li><@s.a action="documentation" title="Parabuld CI Documentation">Documentation</@s.a></li>
                    <li><@s.a action="support" title="Contact Parabuild CI Support">Support</@s.a></li>
                </ul>
            </li>

            <#-- Preferences -->
            <#if Session.loggedInUser??>
                <li class="tab normal <#if contextMenu?? && contextMenu = "preferences">selected</#if>">
                    <@s.a action="preferences" title="Preferences">Preferences</@s.a>
                    <ul>
                        <li><@s.a action="videos" title="%{getText('Send.and.receive.video.messages')}">
                                <span><@s.text name="Video.Messages"/></span> </@s.a></li>
                        <li><@s.a action="contacts" title="%{getText('Invite.and.edit.contacts')}"><@s.text name="Contacts"/></@s.a></li>
                        <li><@s.a action="groups" title="%{getText('Manage.my.groups')}"><@s.text name="Groups"/></@s.a></li>
                        <li><@s.a action="editUserProfile" title="%{getText('manage.my.profile')}"><@s.text name="My.Profile"/></@s.a></li>
                    </ul>
                </li>
            </#if>

            <#-- Login / Logout -->
            <li class="tab last">
                <#if Session.loggedInUser??>
                    <@s.a action="logout" title="Logout from Parabuild">Logout</@s.a>
                <#else>
                    <@s.a action="login" title="Login to Parabuild">Login</@s.a>
                </#if>
            </li>
        </ul>
        <#--
        <ul id="breadcrumb">
           <li><a href="">Home</a></li>
           <li><a href="">Grandparent</a></li>
           <li><a href="">Parent</a></li>
           <li>Current page</li>
        </ul>
        -->
    </div>
    <div id="content">
        ${body}
    </div>

    <div id="footer">
        <ul class="links">
            <li><@s.a action="index" title="Builds">Builds</@s.a></li>
            <li><@s.a action="result/groups" title="Results">Results</@s.a></li>
            <li><@s.a action="search" title="Search">Search</@s.a></li>
            <li><@s.a action="support" title="Support">Support</@s.a></li>
            <li><@s.a action="about" title="About">About</@s.a></li>
        </ul>
        <div class="legal">
            <div class="copyright">Copyright 2004-2019 Parabuild CI. All rights reserved.</div>
        </div>
    </div>
</div>

<#-- Include a GWT history iframe to enable full GWT history support (the id must be exactly as shown) -->
<iframe src="javascript:''" id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>

</body>
</html>