import requests
import boto3
import json


def getNearestRestaurants(latitude, longitude, radius="50"):
    url = "https://api.yelp.com/v3/businesses/search"

    querystring = {}
    querystring["radius"] = radius
    querystring["latitude"] = latitude
    querystring["longitude"] = longitude

    headers = {
        'Authorization': "Bearer eXaRaN4aLw2kSPnmvJ6Ak1DDLdHv7_IT4_9jq8DSCxviyJ0L_Mm-ulOHN_Us5pQiQgGrQQM1zncvG4WrJE0rpYzHViuV2Gh4fmBVz80k4caGm6U-QA-aJuEUKC0YXXYx",
        'User-Agent': "PostmanRuntime/7.15.0",
        'Accept': "*/*",
        'Cache-Control': "no-cache",
        'Host': "api.yelp.com",
        'accept-encoding': "gzip, deflate",
        'Connection': "keep-alive",
        'cache-control': "no-cache"
    }

    response = requests.request(
        "GET", url, headers=headers, params=querystring)
    jsonResponse = response.json()["businesses"]

    restaurants = []
    for res in jsonResponse:
        restaurants.append(res["name"])

    return restaurants


def getRestaurantDetails(name, latitude, longitude):
    url = "https://maps.googleapis.com/maps/api/place/textsearch/json"

    querystring = {}
    querystring["query"] = name
    querystring["location"] = latitude + "," + longitude
    querystring["key"] = "AIzaSyD-Un1y88gOlrRyOo1iNdWDm4-DaYh-41A"

    response = requests.request("GET", url, params=querystring)
    jsonResponse = response.json()

    return jsonResponse["results"][0]


def reviewSentimentAnalysis(text):
    aws_access_key = 'AKIAIXZST2MGI7HCQORQ'
    aws_secret_key = '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'
    comprehend = boto3.client(service_name='comprehend', aws_access_key_id=aws_access_key,
                                  aws_secret_access_key=aws_secret_key, region_name='us-east-2')
                    
    #text = "It is raining today in Seattle"
    print('Calling DetectSentiment')
    jdata = comprehend.detect_sentiment(Text=text, LanguageCode='en')
    #print(json.dumps(jdata))
    return jdata
    # print('End of DetectSentiment\n')
    # rend = JsonResponse(jdata,safe=False)
    # return render(request, 'api1.html',{'rend': json.loads(rend)})