import requests


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
