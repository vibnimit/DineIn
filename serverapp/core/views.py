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


    def menu_match(self,review):
        item_map = {'idli':0,'upma':0}
        review = review.lower()
        #if 'dosa' in review:
        #item_map['dosa']+= review.count('dosa')
        #elif 'upma' in review or 'sambar' in review:
        item_map['upma']+= review.count('upma')
        
        item_map['idli']+= review.count('idly')
        item_map['idli']+= review.count('idli')

        return 'idli' if item_map['idli'] >= item_map['upma'] else 'upma'


    def list(self,request):
        review_list = []

        reviews = [
            "Best place to go for Indian breakfast. Crowded during weekend but still they manage really well by taking names as you go in and make sure people are given a table within some time Service is also very quick. Very tasty food. There is enough parking too",
            "Food was great. But was a mad rush to be seated. Had to wait for a long time, 20 minutes. Staff are courteous and even served us after being seated down. Good place for South Indian food. Sambar could be better.",
            "This used to be one of my favorite place and I guess I still can't complain about the food. This rating is for rude management, servers and bad ambience. We went with our one year old and as with any child he didn't want to sit in one place all the time. The waiters were super rude. I have been to many eateries at bay area with our son and never heard complaints. Definitely not kid friendly. Never going back.",
            "I've been here quite a few times and finally decided to write a review for this place. I've been in some what of a dosa frenzy lately and have been trying out many different dosa places trying to find my favorite one and lucky for me, this place which is the close to home for me, is my favorite!",
            "I always get their paper masala dosa (5/5). I'm no expert in southern Indian food and by no means claim that this is the most authentic, but what I can say is that their dosa and their chutneys were delicious. And just judging by the sheer number of both non-Indian and Indian people here, I can't be the only one who thinks that. Of all the places I've tried, their chutneys are some of the best. My favorite one is the tomato-y one. It pairs perfectly with the crispy dosa. Their prices are super affordable and one dosa is definitely enough to fill you up. Even if it isn't, you wouldn't be breaking the bank ordering a second one. ",
            "Unfortunately, this place is always stupidly crowded and the seating situation/ordering situation is really awkward (-1 star). You order right when you walk in. Half the time I'm not quite sure where the line is or who is and who isn't in line because the front area is also the waiting area for seating and take out orders. The seating is given out pretty randomly. They kinda just walk around asking every party how many people you have and if there's an open spot for that number of people, they'll seat you. I noticed that they tend to seat the people closer to the restaurant area first, so a lot of people shove their way that way. If they had a better system, I'd definitely bump this place up to 5 stars!",
            "My friend and I were in the area and wanted Indian, so I yelped and found this place. Even on a Sunday evening around 8 pm, this place was busy.",
            "The way it works is: you order at the counter first and wait to be seated after ordering. You don't seat yourself, even after ordering at the counter. There were a couple people in front of us in line, so we were able to order pretty quickly.",
            "Vegetable biryani ($8.50): this was a little bit too spicy for me so I didn't eat any, but my friend (who is Indian) didn't like it either Channa bhatura ($7.99): bhatura is a type of bread that is inflated, a little like a balloon. We got it with channa (chickpea) curry. This was my favorite, as the bhatura had a great chewy consistency and the chickpea curry was delicious Masala dosa ($7.99): this was just okay for me but my Indian friend thought it was bomb. It's longer than most regular dosas and came with a good amount of masala potatoes as a filling. This isn't a fancy place by any means. You have to get your own water, for example. But we both enjoyed the food here!", "One of the best South Indian restaurants I have ever been to. Whenever we crave for South Indian delicacies, we find ourselves here for brunch and there is always a crowd and wait time here. From the number of reviews this place has, you can tell how popular it is!",
            "Dosa is very good. I like their chai tea too. If you order tea, unless you specify to server, they don't bring it to you till the end. There is water station and utensils are there. Bathroom hand washing area is outside of the bathroom itself. Not good if you want to see yourself or check your clothing discretely. If you need more coconut chutney, you can ask for it. Food is a bit spicy. ",
            "I now recommended people to come here because of wait is finally reasonable and food are great!", "Tried:Paper dosa- 3/5 was a little too crispy and the chutneys were very bad. Sambar was very average as well, Upma was bad..it was on the sweeter side. Enough to share between 2 people. Masala vada- 4/5 the vada itself was good but the chutneys (coconut, onion, and some green chutney) we're pretty bad. Note this is more like a bajji (crunchy), not actual vada.", "Been here several times, weekday mornings and evenings are pretty packed with people waiting not only inside the restaurant but also outside.Ambience is very casual but the service is quick. Very good South Indian food in great price. I've had dosas, uttapams, idli and vadas and all tasted fresh and nice. These are served with sambhar, coconut chutney and 2 more chutneys. Not sure what those are but taste really good. Extra sambhar can be requested. Overall, I found it to be good for breakfast.", "Popular South Indian Breakfast place in Bay Area. I would have given one star up , if the wait time is less. This place is very busy on weekends. Expect a wait time of 30+ mins to get seated. The wait queue is kind of noisy too !  I have ordered Paper Dosa , Idly , Masala Vada and Filter Coffee. Paper Dosa was too crispy and the chutney was watery. Masala Vada tasted good with sambar. Filter Coffee wasn't too bad but disappointing .", "Idly was really bad", "Madras Cafe is a fairly common name in the Bay and now I know why :-) I've been here ample times and am yet to see a weekend where this place isn't packed to the hilt. As the name suggests, this place is your go-to place for South Indian cuisine. Amongst my favorites and recommendations, I'd call out the Medu Vada, Sada dosa and the filter coffee. The service is earnest and fast and it astonishes me to know that these guys can still get your food to your table within 10 minutes without knowing where you sit. ",
            "The one minor thing I'd definitely call out is probably a minor lack of hygiene. Then again the food more than compensates for that", "I had Idly and vada. Idly was hard and the chutney was tasteless", "We came here for lunch and glad we did. I really liked how they first make you order, pay, seat you and then serve you. While it is a fast food joint, I really like how they serve you with stainless steel plates and flatware. The Kari Ghee Sada Dosa was really good. The Mysore Masala Ghee dosa was ok. I loved the filtered coffee. That was my favorite. The idlis are super soft and fluffy. The pongal is a comfort food made with lentils, rice and ghee. Overall, it was really good.", "I visited the restaurant last week and had a pleasant experience. Upma was great, loved it !", "Just loved the Upma there]"
        ]
        for review in reviews:
            res = externalResources.reviewSentimentAnalysis(review)
            #review_list.append(review)
            review_map = dict()
            review_map['review'] = review
            review_map['item'] = self.menu_match(review)
            review_map['sentiment'] = {'Sentiment':res['Sentiment'],
                'SentimentScore':res['SentimentScore']}
            review_list.append(review_map)

        return Response(review_list)
