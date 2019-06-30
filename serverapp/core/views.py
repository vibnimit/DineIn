# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from resources import externalResources
import json

class RestaurantsView(viewsets.ViewSet):

    def list(self, request):
        print(request.GET["latitude"])
        res = externalResources.getNearestRestaurants(
            request.GET["latitude"], request.GET["longitude"])
        return Response(res)



class SentimentAnalysisViewSet(viewsets.ViewSet):

    def list(self,request):
        review_list = []
        reviews = [
            "Best place to go for Indian breakfast. Crowded during weekend but still they manage really well by taking names as you go in and make sure people are given a table within some time Service is also very quick. Very tasty food. There is enough parking too",
            "Food was great. But was a mad rush to be seated. Had to wait for a long time, 20 minutes. Staff are courteous and even served us after being seated down. Good place for South Indian food. Sambar could be better.",
            "This used to be one of my favorite place and I guess I still can't complain about the food. This rating is for rude management, servers and bad ambience. We went with our one year old and as with any child he didn't want to sit in one place all the time. The waiters were super rude. I have been to many eateries at bay area with our son and never heard complaints. Definitely not kid friendly. Never going back."
        ]
        for review in reviews:
            res = externalResources.reviewSentimentAnalysis(review)
            #review_list.append(review)
            review_map = dict()
            review_map['review'] = review
            print res
            review_map['sentiment'] = {'Sentiment':res['Sentiment'],
                'SentimentScore':res['SentimentScore']}
            review_list.append(review_map)

        return Response(review_list)
