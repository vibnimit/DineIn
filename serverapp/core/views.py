# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from resources import externalResources

# Create your views here.


class RestaurantsView(viewsets.ViewSet):

    def list(self, request):
        print(request.GET["latitude"])
        res = externalResources.getNearestRestaurants(
            request.GET["latitude"], request.GET["longitude"])
        return Response(res)
