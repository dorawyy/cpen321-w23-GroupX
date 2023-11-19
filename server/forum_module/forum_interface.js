import express from "express";
import { MongoClient } from "mongodb";
const uri = 'mongodb://127.0.0.1:27017';
const client = new MongoClient(uri);

// import client from "./server/mongoClient.js"

//const https = require("https");

// const url = "http://" + host + ":" + forumDB_port  + "/forums";
//ChatGPT usage: No
function dateAdded(){
	const date = new Date();
	let time = date.toLocaleDateString() + " " + date.toLocaleTimeString();	
	return time;
}

export class ForumModule{
    //ChatGPT usage: No
    construtor(){
        this.dateCreated = new Date();
    }

    // DATABASE COMMUNICATION INTERFACES
    //ChatGPT usage: No
    createForum = async function(forumId, forumName){
        try{
            let forum = {
                id: forumId,
                name : forumName,
            };
            forum.dateCreated = dateAdded();
            forum.comments = [];
            const res =  await client.db('ForumDB').collection('forums').insertOne(forum);
            return forum;
        }catch(err){
            return (err);
        }
    }

    //ChatGPT usage: No
    getAllForums = async function(){
        try{
            const result = await client.db("ForumDB").collection("forums").find().sort({ 'rating' : -1 }).toArray();
            return (result);
        } catch (err){
            return(err)
        }
    }
    //ChatGPT usage: No
    getForum = async function (forumId){
        // const response = await axios.get(url  + "/" + forumId);
        try{
            
            const result = await client.db("ForumDB").collection("forums").find({"id": forumId}).toArray();
            // console.log(result);

            return (result);
        } catch (err){
            return(err)
        }
        // return response.data;
        
    }

    //ChatGPT usage: No
    deleteForum = async function(forumId){
        // const response = await axios.delete(url + "/" +forumId);
        // return response.data;

        try{
            const result = await client.db("ForumDB").collection("forums").deleteOne({id : forumId});
            return result.acknowledged
    
        }catch(err){
            return(err);
        }
        
    }

    //ChatGPT usage: No
    deleteForums = async function(){
        // const response = await axios.delete(url);
        // return response.data;

        try{
            const result = await client.db('ForumDB').collection('forums').drop({});
            
            return result
    
        }catch(err){
            return (err)
        }
    }

    //ChatGPT usage: No
    addCommentToForum = async function(forumId, commentData, username, parent_id){
        try{
            var datePosted = dateAdded();
            let date = new Date();
            let comment = {
                comment_id : forumId + "_" + date,
                parent_id : parent_id, // NULL if it has no parent
                username : username,
                content : commentData,
                datePosted: datePosted
            }
            const response = await client.db('ForumDB').collection('forums')
                            .updateOne({ id : forumId}, { $push:{ comments : comment }});
            return response.acknowledged;
        }catch(err){
            console.log(err);
            return false;
        }

    }
};
