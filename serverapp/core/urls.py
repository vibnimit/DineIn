
from django.conf.urls import url, include
# from django.urls import path, include
from django.contrib import admin
from core.views import *
from rest_framework.routers import DefaultRouter, SimpleRouter

router = SimpleRouter()
router.register(r'getList', RestaurantsView, base_name="getRestaurants")
router.register(r'getSentimentScores', SentimentAnalysisViewSet, base_name="sentiment-scores")

