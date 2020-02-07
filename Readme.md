Application can:
Generate static and dynamic reports, as well as changing user rights to files and folders.
Files available not only to users of your domain, but also to strange people, will be displayed.

Works good if you want to be sure that your files shared correctly!
 
## How to
1.
https://console.developers.google.com/
Create a project.
Enable API:
 Admin SDK		
 Apps Activity API		
 Google Ads API		
 Google Drive API	 
 Subscribe with Google Developer AP
 
2.Generate service account. Download a json.

3. Resources folder contains folder_exceptions.json. 
If your root path contains a huge data, you can skip some folders.

4. Resources folder contains email_exceptions.json. 
If you are sure that an account with a strange domain must have permissions to a file - add it to the list.