
               Subversion, a version control system.
               =====================================

$LastChangedDate: 2005-09-10 02:03:59 +1000 (Sat, 10 Sep 2005) $

Contents:

     I. A FEW POINTERS
    II. DOCUMENTATION
   III. PARTICIPATING IN THE SUBVERSION COMMUNITY
    IV. QUICKSTART GUIDE
     V. CONVERTING FROM CVS


I.    A FEW POINTERS

      For an overview of the Subversion project, visit

         http://subversion.tigris.org/

      Once you have a Subversion client you can get the latest version
      of the code with the command:

         $ svn co http://svn.collab.net/repos/svn/trunk subversion



II.   DOCUMENTATION

      The main documentation is the Subversion Book, written in
      DocBook XML, which lives at http://svn.red-bean.com/svnbook.  If
      you wish to build the documentation from source, read
      src/en/README.  Otherwise, an on-line version of the book can be
      found at http://svnbook.red-bean.com.

      See COPYING for copyright information.
      See INSTALL for installation information.
      See www/hacking.html for development information.



III.  PARTICIPATING IN THE SUBVERSION COMMUNITY

      First, read www/hacking.html!  It describes Subversion coding and
      log message standards, as well as how to join discussion lists.

      Talk on IRC with developers:  irc.freenode.net, channel #svn.

      Read the FAQ:  http://subversion.tigris.org/faq.html



IV.   QUICKSTART GUIDE

      Please note that this section is just a quick example for people
      who want to see Subversion run immediately.  It's not an excuse
      to ignore the book!

      The Subversion client has an abstract interface for accessing a
      repository.  Three "Repository Access" (RA) implementations
      currently exist as libraries:

      libsvn_ra_dav:   accesses a networked repository using WebDAV.
      libsvn_ra_local: accesses a local repository using Berkeley DB.
      libsvn_ra_svn:   accesses a remote repository using a custom protocol.

      You can see which methods are available to your 'svn' client by
      running 'svn --version'.  The following example assumes that
      ra_local is available to your client.  (If you don't see
      ra_local, it probably means that Berkeley DB wasn't found when
      compiling your client binary.)


     1.  svnadmin create /path/to/repos    
  
          - this creates a new directory, 'repos'.  Make sure that
            /path/to/repos/ is on local disk, NOT a network share.
 
          - make SURE you have complete recursive read/write access to
            the newly created 'repos' directory.

          - understand that the repository is mainly a collection of
            Berkeley DB files;  you won't actually see your versioned
            data if you peek in there.

     2.  svn import /tmp/project file:///path/to/repos -m "Initial import"

          - /tmp/project is a tree of data you've pre-arranged.
            If you can, use this layout, as it will help you later on:

               /tmp/project/branches/
               /tmp/project/tags/
               /tmp/project/trunk/
                               foo.c
                               bar.c
                               baz.c
                               etc.

     3. svn checkout file:///path/to/repos/trunk project

          - this creates a 'project' directory which is a working copy of
            the /trunk directory in the repository.

     4. Try using the repository:

          - edit a file in your working copy.
          - run 'svn diff' to see the changes.
          - run 'svn commit' to commit the changes.
          - run 'svn up' to bring your working copy up-to-date.

        Be sure to read chapters 2 and 3, they're a critical
        introduction to the svn commandline client.

     5. Get a real server process running (either apache or svnserve)
        so that your repository can be made available over a network.
        Read chapters 5 and 6 to learn about how to administer a
        repository and how to set up a server process.

      *** NEWBIES BEWARE:

      The absolute most common stumbling block for newbies is problems
      with permissions and ownership on the repository.  Any process
      that opens the repository must have complete read/write access
      to it.  This goes for any tool ('svnadmin', 'svnlook') or any
      server process (apache, svnserve), or your own svn client, if
      it's accessing via file:///.

      Look at the last section in chapter 6 to understand how to tweak
      repository ownership and permissions for multiple users and
      processes.



V.   CONVERTING FROM CVS

     If you're a CVS user trying to move your CVS history over to
     Subversion, then be sure to visit the 'cvs2svn' project:

          http://cvs2svn.tigris.org

     You can get the latest released version of the cvs2svn converter
     from the project downloads area:

          http://cvs2svn.tigris.org/servlets/ProjectDocumentList?folderID=2976

     Please note that the cvs2svn project is a *separate* project from
     Subversion.  If you have problems with cvs2svn or are confused,
     please email the cvs2svn project's mailing lists, not the
     Subversion lists.

     Finally, be sure to see Appendix A in the Subversion book.  It
     contains a very quick overview of the major differences between
     CVS and Subversion.
