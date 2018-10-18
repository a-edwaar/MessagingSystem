# Messaging system

### (1) Register

* To implement the register function I used communication between the client and server classes before actually starting the individual sender and receiver threads. This way i would still be able to pass in the client name to the threads and i would be able to catch any errors, for example if the user was already registered.

- I did this by creating corresponding while loops in both the client and server till a client has registered correctly
- The client enters register followed by their name and then the server checks if that name is present in the message table already
- If the name is not present in the message table:
  - The server adds the name to the message table
  - Sends a message to let the client know
  - The client notifies the user everything is good
  - Both classes break their corresponding while loops and start their threads.
- If the name is in the message table:
  - The server lets the client know
  - The client notifies the user that the name to register can't be registered again
  - Both classes continue to loop till a valid registration.
- I decided to let the users log in automatically after registration as it seems for me to be a hassle to log in after registration.

### (2) Login

* To implement login I added to my solution for register in the while loops in the client and server class.

- The server receives the request to login with a username and checks if the name is in the message table
- if so :
  - The server will login
  - Notifies the client
  - The client will notify the user
  - The threads will begin
- If not :
  - Notifies the client
  - Client notifies the user
  - Keeps looping
- I wanted to allow multiple logins to occur. For this to work I had to change 2 things in my design.
  - 1) I changed having one user to one blocking queue in the message table to one user with an array list of blocking queues.
    - This allows me to send messages to multiple users logged in using the same account by traversing the array list.
  - 2) When a user logs in either after registration or log in I send a unique identifier
      - This is a number and is incremented as a new client connects to the server.
      - This is then passed into the threads so even if there are many users logged into the same account each thread will know the individual user it belongs to making it easier to traverse messages without affecting other threads.
  - I also get the latest message and send to the client however I'll talk about this when i discuss how i stored messages.

### (3) Logout

* To implement logout it was similar to quit however i didn't want to delete the user all together from the client table so I would be able to log in with same user again or if other users were still using that account they wouldn't be affected.

- To implement this I had to change my design slightly by adding another hash map which stores the usernames as the keys and then has an array list of type Message to store all messages sent to a client.
  - Therefore even if a user is logged out, in the server receiver it not only traverses the blocking queues of that user and offers the msg to each one but also adds the msg to the clients array list.

- How it works :
  - 'logout' is typed in by the user
  - The client then sets the recipient as themselves and sets a boolean to end the while loop.
  - The server receiver receives the message and adds the unique identifier to the end of 'logout' so the server will be able to tell which particular one of the clients to disconnect from.
  - The server receiver then ends its loop.
  - The server sender of all users logged in with that account will receive this message
  - But only the server sender who has the same logout message (with the uid attached) as the server receiver will act on the message:
    - Deletes the message from the clients message list as we don't want it as a proper message.
    - Sends the message to the particular client it is attached to.
    - Ends the while loop.
    - Sends a message on the server to show the user logged out.
  - The client receiver then receives the message and checks if it has it's uid attached at the end. If so, it breaks the while loop and notifies the user it has logged out.

### (4) Keeping all messages received by the user

* I have already shown how i implemented storing the messages in my logout solution.
* To implement a counter for current index :
  - I created an index when the server sender is started which assigns it to the last msg in the array list of messages.
  - If a new message is sent I set it again to the last msg of the array.
    - I don't just increment it as it is can be altered differently elsewhere.
* To show the last message received when the client logs in I call a method from the server that I created in the message table class which gets the last message in the message list and prints to the client which outputs to the user.

### (5) Previous

* To implement 'previous' it is a similar method to when i type 'logout' by adding a uid however i do not end any of the threads.

- The server sender with that client name will receive the message
- Only the sender with the uid matching the one in the message sent will react
  - Removes the message from the message list because we don't need in there
  - If the current msg index is not pointing at the first element of the message list, decrement it
  - Send the message at the new index to the client

### (6) Next

* To implement 'next' it is the same method as sending 'previous' however the logic with the current message index is different

- If the current message index is not yet at the end of the message list, increment it
- Then send to the client the message at the new index
- Otherwise send a message depending on the circumstance i.e 'messages empty' or 'no messages after'  

### (7) Delete

* To implement delete with multiple users using under the same account it was harder as the current message index has to change for every client with that name not just the one who is deleting the message.

- My solution for delete was to display the next message after the current is deleted and if no message was found after, i.e it was at the end of the list, it will go to the previous.
-  The current msg index only had to be decremented when it was at the end of the list because the list would automatically shift so the index would point to the next message anyway.
- It then prints to the client like before the current msg.
- I also have as part of the if statement if the message contains the word delete and not delete with the uid added to it. This means all the clients using the same account will go into this loop apart from the one who is its uid that triggered it. In this loop the current msg index decrements to adapt to its new list.

### Self-Chosen Feature

#### Storing users and messages in a file so no loss of information

* To implement this I made two new functions in the message table. One two write to a file and the other to read.
- In the write to file method I wanted the layout of the file to be one line the users name and then the next line all the messages of that user separated by a " ::: ". I chose this because I thought it would be unusual for someone to send something lie that in a message. I could make it more complicated if needs be.
- In the read to file, it was a little more complicated as I could pick up the user as it was a string but I could not cast the messages as of type Message therefore I added each as a string to an array. I then made a new method in the Message class which turns a String into a Message and so I traversed the new string array, turning them into messages and adding them to the message list of the client.
- I put the read file when the server starts to run to load all data
- And I made a Shutdown hook in the server class which will run a thread when the server is closed (through ctrl c). I then put the write method in this thread so whenever the server is closed it will write all the data to the file.

#### Group messages like 'whatsapp'

* To implement this I needed to add a new hash map where I would store the names of the groups and all the members of that group. I put this new map in a new class GroupTable and it works similar to the MessagesTable.

* Syntax for my groups
- Create a new groups
  - group
  - new
  - "group name"
- Add a member to a group
  - group
  - add
  - "user name"
- Send a msg to the group
  - group
  - send
  - "group name"
  - "msg"
- Remove a member from the groups
  - group
  - remove
  - "group name"
  - "user name"
- Delete a group
  - group
  - delete
  - "group name"

- I created helper functions in the group table class to perform certain events.
- Most of the communication was with client sender and the server receiver. The server receiver will check if everything is valid when a user will want to alter a group or create one, i.e check if the group already exists.
- I had to alter how messages were sent slightly in the server receiver so i made an array called recipients.
  - Then if I wanted to send a message to one person I just add that person to the array however if i want to send it to a whole group it will add all the users in that group to the array and iterate through each user passing the message.
  - This also works if an account has two or more users logged in at the same time.
  - I also wanted to make sure clients knew that they were receiving a group message and not just a normal one so I created a flag 'isGroupMsg' and if that flag is true then it will send a message with the group name in brackets.
  - I also wanted to make sure clients knew if they had been added or removed from a group so i created another flag 'alteringGroup' and if that flag is set to true the message will be sent to that client who is been added or removed and their server sender will think its from their client which meant I could add two more if statements and print what I wanted the client to show to the user.
- I also slightly edited the write and read to file when i copied it over to the group table class as I was only storing Strings instead of Messages. And then I called these in the same places as I did with the message table in the server class and the shutdown hook class.



### Git lab link
https://git.cs.bham.ac.uk/axe772/MessagingSystem.git
