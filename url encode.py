def url_encode_spaces(filename):
    with open(filename, 'r') as file:
        content = file.read()
    
    content = content.replace(' ', '%20')
    
    with open(filename, 'w') as file:
        file.write(content)

# Call the function with your filename
url_encode_spaces('Query.txt')
